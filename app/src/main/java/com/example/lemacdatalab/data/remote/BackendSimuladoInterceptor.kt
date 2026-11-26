package com.example.lemacdatalab.data.remote

import android.content.Context
import com.example.lemacdatalab.model.RegistroVersion
import com.example.lemacdatalab.model.RolUsuario
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.File
import java.io.IOException

data class EstadoServidorSimulado(
    val usuarios: MutableList<PerfilDto> = mutableListOf(),
    val consentimientos: MutableList<ConsentimientoDto> = mutableListOf(),
    val catalogos: CatalogosDto = CatalogosDto(),
    val registros: MutableList<RegistroDto> = mutableListOf(),
    val versionesDescartadas: MutableList<RegistroVersion> = mutableListOf(),
    val tokensActivos: MutableMap<String, String> = mutableMapOf(),
    var ultimoIdentificadorAutenticado: String? = null
)

/**
 * Almacenamiento propio del backend simulado.
 * Puede persistir en un archivo JSON propio del dispositivo (sin depender de Room)
 * y cargarse inicialmente desde el dataset sintético en assets.
 */
class AlmacenBackendSimulado(
    private val archivoPersistencia: File? = null,
    jsonSemilla: String? = null,
    private val gson: Gson = GsonBuilder().serializeNulls().create()
) {
    private val estado: EstadoServidorSimulado = cargarEstadoInicial(jsonSemilla)

    @Synchronized
    fun obtenerEstado(): EstadoServidorSimulado = estado

    @Synchronized
    fun guardar() {
        val archivo = archivoPersistencia ?: return
        archivo.parentFile?.mkdirs()
        archivo.writeText(gson.toJson(estado), Charsets.UTF_8)
    }

    @Synchronized
    fun obtenerVersionesDescartadas(idRegistro: Int? = null): List<RegistroVersion> =
        if (idRegistro == null) {
            estado.versionesDescartadas.toList()
        } else {
            estado.versionesDescartadas.filter { it.idRegistro == idRegistro }
        }

    @Synchronized
    fun upsertRegistroConResolutor(entrante: RegistroDto, idForzado: Int? = null): RegistroDto {
        val fechaDiaNormalizada = entrante.fechaDia.ifBlank { entrante.fechaHoraCreacion.take(10) }
        val normalizado = entrante.copy(
            idRegistro = idForzado ?: entrante.idRegistro,
            fechaDia = fechaDiaNormalizada
        )

        val indiceExistente = estado.registros.indexOfFirst { actual ->
            (idForzado != null && actual.idRegistro == idForzado) ||
                    (normalizado.idRegistro > 0 && actual.idRegistro == normalizado.idRegistro) ||
                    (actual.idUsuario == normalizado.idUsuario &&
                            actual.fechaDia == normalizado.fechaDia &&
                            actual.tipoArea == normalizado.tipoArea)
        }

        if (indiceExistente == -1) {
            val nuevoId = when {
                idForzado != null && idForzado > 0 -> idForzado
                normalizado.idRegistro > 0 -> normalizado.idRegistro
                else -> (estado.registros.maxOfOrNull { it.idRegistro } ?: 100) + 1
            }
            val insertado = normalizado.copy(idRegistro = nuevoId)
            estado.registros.add(insertado)
            guardar()
            return insertado
        }

        val existente = estado.registros[indiceExistente]
        val resultado = resolverConflicto(
            local = existente,
            remoto = normalizado.copy(idRegistro = existente.idRegistro)
        )
        estado.versionesDescartadas.add(resultado.versionDescartada)
        estado.registros[indiceExistente] = resultado.ganador
        guardar()
        return resultado.ganador
    }

    @Synchronized
    fun eliminarRegistroConResolutor(idRegistro: Int, modificadoEnUtc: Long): RegistroDto? {
        val indiceExistente = estado.registros.indexOfFirst { it.idRegistro == idRegistro }
        if (indiceExistente == -1) return null

        val existente = estado.registros[indiceExistente]
        val eliminadoRemoto = existente.copy(
            estadoRegistro = "eliminado",
            modificadoEnUtc = modificadoEnUtc
        )
        val resultado = resolverConflicto(local = existente, remoto = eliminadoRemoto)
        estado.versionesDescartadas.add(resultado.versionDescartada)
        estado.registros[indiceExistente] = resultado.ganador
        guardar()
        return resultado.ganador
    }

    private fun cargarEstadoInicial(jsonSemilla: String?): EstadoServidorSimulado {
        val archivo = archivoPersistencia
        if (archivo != null && archivo.exists()) {
            runCatching {
                val texto = archivo.readText(Charsets.UTF_8)
                gson.fromJson(texto, EstadoServidorSimulado::class.java)
            }.getOrNull()?.let { return it }
        }

        val dataset = jsonSemilla?.takeIf { it.isNotBlank() }?.let {
            runCatching { gson.fromJson(it, DatasetSinteticoDto::class.java) }.getOrNull()
        } ?: datasetPorDefecto()

        return EstadoServidorSimulado(
            usuarios = dataset.usuarios.toMutableList(),
            consentimientos = dataset.consentimientos.toMutableList(),
            catalogos = dataset.catalogos,
            registros = dataset.registros.toMutableList()
        ).also {
            if (archivo != null) {
                archivo.parentFile?.mkdirs()
                archivo.writeText(gson.toJson(it), Charsets.UTF_8)
            }
        }
    }

    companion object {
        private const val ARCHIVO_ESTADO = "backend_simulado_estado.json"
        private const val ASSET_DATASET = "dataset_sintetico.json"

        fun desdeContexto(context: Context): AlmacenBackendSimulado {
            val archivo = File(context.filesDir, ARCHIVO_ESTADO)
            val jsonAsset = runCatching {
                context.assets.open(ASSET_DATASET).bufferedReader(Charsets.UTF_8).use { it.readText() }
            }.getOrNull()
            return AlmacenBackendSimulado(archivoPersistencia = archivo, jsonSemilla = jsonAsset)
        }

        fun datasetPorDefecto(): DatasetSinteticoDto = DatasetSinteticoDto(
            usuarios = listOf(
                PerfilDto(
                    idUsuario = 1,
                    identificadorSintetico = "AX-7B99",
                    rol = RolUsuario.USUARIO.name,
                    fechaCreacion = "2026-10-12",
                    objetivoAdicciones = "Mantener abstinencia diaria y aplicar pausas activas ante el estrés"
                ),
                PerfilDto(
                    idUsuario = 2,
                    identificadorSintetico = "ADM-0001",
                    rol = RolUsuario.ADMIN.name,
                    fechaCreacion = "2026-10-12",
                    objetivoAdicciones = null
                ),
                PerfilDto(
                    idUsuario = 3,
                    identificadorSintetico = "EDT-0001",
                    rol = RolUsuario.EDITOR.name,
                    fechaCreacion = "2026-10-12",
                    objetivoAdicciones = null
                ),
                PerfilDto(
                    idUsuario = 4,
                    identificadorSintetico = "BZ-4M12",
                    rol = RolUsuario.USUARIO.name,
                    fechaCreacion = "2026-10-12",
                    objetivoAdicciones = "Reducir consumo impulsivo y contactar red de apoyo ante craving alto"
                )
            ),
            consentimientos = listOf(
                ConsentimientoDto(
                    identificadorSintetico = "AX-7B99",
                    version = "1.0",
                    aceptado = true,
                    fechaAceptacionUtc = 1760270400000L
                ),
                ConsentimientoDto(
                    identificadorSintetico = "ADM-0001",
                    version = "1.0",
                    aceptado = true,
                    fechaAceptacionUtc = 1760270400000L
                ),
                ConsentimientoDto(
                    identificadorSintetico = "EDT-0001",
                    version = "1.0",
                    aceptado = true,
                    fechaAceptacionUtc = 1760270400000L
                ),
                ConsentimientoDto(
                    identificadorSintetico = "BZ-4M12",
                    version = "1.0",
                    aceptado = false,
                    fechaAceptacionUtc = null
                )
            ),
            catalogos = CatalogosDto(),
            registros = listOf(
                RegistroDto(
                    idRegistro = 101,
                    idUsuario = 1,
                    tipoArea = "animo",
                    fechaHoraCreacion = "2026-10-28 09:15:00",
                    fechaDia = "2026-10-28",
                    notasOpcionales = "Jornada tranquila de trabajo",
                    estadoRegistro = "confirmado",
                    modificadoEnUtc = 1761642900000L,
                    nivelIntensidad = 2,
                    contexto = "Ámbito Laboral",
                    depresion = 1,
                    elevacion = 2,
                    horasSueno = 7,
                    altoRiesgo = false,
                    sintomasPsicoticos = false
                ),
                RegistroDto(
                    idRegistro = 102,
                    idUsuario = 1,
                    tipoArea = "dbt",
                    fechaHoraCreacion = "2026-10-28 19:30:00",
                    fechaDia = "2026-10-28",
                    notasOpcionales = "Práctica de respiración consciente",
                    estadoRegistro = "confirmado",
                    modificadoEnUtc = 1761679800000L,
                    habilidad = "Mindfulness",
                    intensidadEmocional = 1
                ),
                RegistroDto(
                    idRegistro = 103,
                    idUsuario = 1,
                    tipoArea = "neurodesarrollo",
                    fechaHoraCreacion = "2026-10-29 11:00:00",
                    fechaDia = "2026-10-29",
                    notasOpcionales = "Dividí el informe en bloques de 20 minutos",
                    estadoRegistro = "confirmado",
                    modificadoEnUtc = 1761735600000L,
                    tipoEntrada = "Tarea",
                    intensidadNeuro = 2,
                    estrategia = "Lista de pasos",
                    resultadoPercibido = "Me ayudó"
                ),
                RegistroDto(
                    idRegistro = 104,
                    idUsuario = 1,
                    tipoArea = "adicciones",
                    fechaHoraCreacion = "2026-10-29 21:00:00",
                    fechaDia = "2026-10-29",
                    notasOpcionales = "Caminata corta al terminar el día",
                    estadoRegistro = "confirmado",
                    modificadoEnUtc = 1761771600000L,
                    craving = 1,
                    desencadenante = "Estrés o sobrecarga",
                    accionProtectora = "Salir a caminar o ejercicio",
                    estadoObjetivo = "Cumplido"
                )
            )
        )
    }
}

/**
 * Interceptor OkHttp que simula un servidor REST/JSON dentro de la app.
 * No requiere red ni localhost, persiste en su propio almacenamiento y aplica
 * «última modificación gana» conservando las versiones descartadas.
 */
class BackendSimuladoInterceptor(
    val almacen: AlmacenBackendSimulado = AlmacenBackendSimulado(),
    private val gson: Gson = GsonBuilder().serializeNulls().create()
) : Interceptor {

    @Volatile
    var sinConexionSimulada: Boolean = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (sinConexionSimulada) {
            throw IOException("Sin conexión con el servidor simulado")
        }

        val metodo = request.method.uppercase()
        val segmentos = request.url.pathSegments.filter { it.isNotBlank() }

        return when {
            metodo == "POST" && segmentos == listOf("login") ->
                manejarLogin(request)

            metodo == "GET" && segmentos == listOf("consentimiento") ->
                manejarObtenerConsentimiento(request)

            metodo == "POST" && segmentos == listOf("consentimiento", "aceptacion") ->
                manejarAceptarConsentimiento(request)

            metodo == "GET" && segmentos == listOf("catalogos") ->
                respuestaJson(request, 200, almacen.obtenerEstado().catalogos)

            metodo == "GET" && segmentos == listOf("perfil") ->
                manejarObtenerPerfil(request)

            metodo == "GET" && segmentos == listOf("registros") ->
                manejarObtenerRegistros(request)

            metodo == "POST" && segmentos == listOf("registros") ->
                manejarCrearRegistro(request)

            metodo == "PUT" && segmentos.size == 2 && segmentos[0] == "registros" ->
                manejarActualizarRegistro(request, segmentos[1].toIntOrNull())

            metodo == "DELETE" && segmentos.size == 2 && segmentos[0] == "registros" ->
                manejarEliminarRegistro(request, segmentos[1].toIntOrNull())

            metodo == "GET" && segmentos == listOf("cambios") ->
                manejarObtenerCambios(request)

            else -> respuestaError(request, 404, "Endpoint no encontrado: $metodo /${segmentos.joinToString("/")}")
        }
    }

    private fun manejarLogin(request: Request): Response {
        val body = leerBody<LoginRequestDto>(request)
            ?: return respuestaError(request, 400, "Cuerpo de login inválido")
        val identificador = body.identificadorSintetico.trim().uppercase()
        if (identificador.isBlank()) {
            return respuestaError(request, 400, "El identificador sintético es obligatorio")
        }

        val estado = almacen.obtenerEstado()
        val perfil = synchronized(almacen) {
            estado.usuarios.firstOrNull { it.identificadorSintetico.equals(identificador, ignoreCase = true) }
        } ?: return respuestaError(request, 401, "ID no registrado en el sistema")

        val token = "tok-lemac-${perfil.identificadorSintetico}-${System.currentTimeMillis()}"
        synchronized(almacen) {
            estado.tokensActivos[token] = perfil.identificadorSintetico
            estado.ultimoIdentificadorAutenticado = perfil.identificadorSintetico
            almacen.guardar()
        }

        val respuesta = LoginResponseDto(
            token = token,
            idUsuario = perfil.idUsuario,
            identificadorSintetico = perfil.identificadorSintetico,
            rol = perfil.rol,
            fechaCreacion = perfil.fechaCreacion,
            objetivoAdicciones = perfil.objetivoAdicciones
        )
        return respuestaJson(request, 200, respuesta)
    }

    private fun manejarObtenerConsentimiento(request: Request): Response {
        val estado = almacen.obtenerEstado()
        val identificador = request.url.queryParameter("identificador")?.trim()?.uppercase()
            ?: identificadorDesdeHeader(request)
            ?: estado.ultimoIdentificadorAutenticado
            ?: "AX-7B99"

        val consentimiento = synchronized(almacen) {
            estado.consentimientos.firstOrNull {
                it.identificadorSintetico.equals(identificador, ignoreCase = true)
            } ?: ConsentimientoDto(identificadorSintetico = identificador, aceptado = false)
        }
        return respuestaJson(request, 200, consentimiento)
    }

    private fun manejarAceptarConsentimiento(request: Request): Response {
        val body = leerBody<AceptacionConsentimientoRequestDto>(request)
            ?: return respuestaError(request, 400, "Cuerpo de aceptación inválido")
        val identificador = body.identificadorSintetico.trim().uppercase()
        if (identificador.isBlank()) {
            return respuestaError(request, 400, "El identificador sintético es obligatorio")
        }

        val actualizado = ConsentimientoDto(
            identificadorSintetico = identificador,
            version = body.version,
            aceptado = true,
            fechaAceptacionUtc = body.fechaAceptacionUtc
        )

        synchronized(almacen) {
            val estado = almacen.obtenerEstado()
            val idx = estado.consentimientos.indexOfFirst {
                it.identificadorSintetico.equals(identificador, ignoreCase = true)
            }
            if (idx >= 0) {
                estado.consentimientos[idx] = actualizado
            } else {
                estado.consentimientos.add(actualizado)
            }
            almacen.guardar()
        }
        return respuestaJson(request, 200, actualizado)
    }

    private fun manejarObtenerPerfil(request: Request): Response {
        val estado = almacen.obtenerEstado()
        val idUsuarioParam = request.url.queryParameter("idUsuario")?.toIntOrNull()
        val identificadorParam = request.url.queryParameter("identificador")?.trim()?.uppercase()
            ?: identificadorDesdeHeader(request)
            ?: estado.ultimoIdentificadorAutenticado

        val perfil = synchronized(almacen) {
            when {
                idUsuarioParam != null ->
                    estado.usuarios.firstOrNull { it.idUsuario == idUsuarioParam }
                !identificadorParam.isNullOrBlank() ->
                    estado.usuarios.firstOrNull {
                        it.identificadorSintetico.equals(identificadorParam, ignoreCase = true)
                    }
                else -> estado.usuarios.firstOrNull()
            }
        } ?: return respuestaError(request, 404, "Perfil no encontrado")

        return respuestaJson(request, 200, perfil)
    }

    private fun manejarObtenerRegistros(request: Request): Response {
        val idUsuarioParam = request.url.queryParameter("idUsuario")?.toIntOrNull()
        val lista = synchronized(almacen) {
            almacen.obtenerEstado().registros
                .filter { idUsuarioParam == null || it.idUsuario == idUsuarioParam }
                .sortedWith(compareByDescending<RegistroDto> { it.fechaHoraCreacion }.thenByDescending { it.idRegistro })
        }
        return respuestaJson(request, 200, lista)
    }

    private fun manejarCrearRegistro(request: Request): Response {
        val entrante = leerBody<RegistroDto>(request)
            ?: return respuestaError(request, 400, "Registro inválido")
        if (!entrante.tieneEscalaValida()) {
            return respuestaError(request, 400, "Valor fuera de la escala 0 a 4")
        }
        val guardado = almacen.upsertRegistroConResolutor(entrante)
        return respuestaJson(request, 200, guardado)
    }

    private fun manejarActualizarRegistro(request: Request, idRegistro: Int?): Response {
        if (idRegistro == null) {
            return respuestaError(request, 400, "ID de registro inválido")
        }
        val entrante = leerBody<RegistroDto>(request)
            ?: return respuestaError(request, 400, "Registro inválido")
        if (!entrante.tieneEscalaValida()) {
            return respuestaError(request, 400, "Valor fuera de la escala 0 a 4")
        }
        val actualizado = almacen.upsertRegistroConResolutor(entrante, idForzado = idRegistro)
        return respuestaJson(request, 200, actualizado)
    }

    private fun manejarEliminarRegistro(request: Request, idRegistro: Int?): Response {
        if (idRegistro == null) {
            return respuestaError(request, 400, "ID de registro inválido")
        }
        val modificadoEnUtc = request.url.queryParameter("modificadoEnUtc")?.toLongOrNull()
            ?: System.currentTimeMillis()
        val resultado = almacen.eliminarRegistroConResolutor(idRegistro, modificadoEnUtc)
            ?: return respuestaError(request, 404, "Registro no encontrado")
        return respuestaJson(request, 200, resultado)
    }

    private fun manejarObtenerCambios(request: Request): Response {
        val desdeUtc = request.url.queryParameter("desde")?.toLongOrNull() ?: 0L
        val idUsuarioParam = request.url.queryParameter("idUsuario")?.toIntOrNull()
        val cambios = synchronized(almacen) {
            almacen.obtenerEstado().registros
                .filter { it.modificadoEnUtc > desdeUtc }
                .filter { idUsuarioParam == null || it.idUsuario == idUsuarioParam }
                .sortedBy { it.modificadoEnUtc }
        }
        return respuestaJson(request, 200, cambios)
    }

    private fun identificadorDesdeHeader(request: Request): String? {
        val header = request.header("Authorization")?.removePrefix("Bearer")?.trim() ?: return null
        return synchronized(almacen) {
            almacen.obtenerEstado().tokensActivos[header]
        }
    }

    private inline fun <reified T> leerBody(request: Request): T? {
        val body = request.body ?: return null
        return runCatching {
            val buffer = Buffer()
            body.writeTo(buffer)
            val json = buffer.readUtf8()
            gson.fromJson(json, T::class.java)
        }.getOrNull()
    }

    private fun respuestaJson(request: Request, codigo: Int, payload: Any): Response {
        val json = gson.toJson(payload)
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(codigo)
            .message(if (codigo in 200..299) "OK" else "Error")
            .body(json.toResponseBody(MEDIA_TYPE_JSON))
            .build()
    }

    private fun respuestaError(request: Request, codigo: Int, mensaje: String): Response =
        respuestaJson(request, codigo, mapOf("error" to mensaje))

    companion object {
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}
