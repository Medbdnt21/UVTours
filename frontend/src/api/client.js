// Librería centralizada de comunicación con el backend (equivalente a libservlet.js).
// Todas las llamadas fetch pasan por aquí: se inyecta el token JWT y se normalizan errores.

const TOKEN_KEY = 'uvtours_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

async function request(method, path, body, { auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth) {
    const token = getToken()
    if (token) headers['Authorization'] = `Bearer ${token}`
  }

  const options = { method, headers }
  if (body !== undefined) options.body = JSON.stringify(body)

  const response = await fetch(`/api${path}`, options)

  if (response.status === 204) return null

  const datos = await response.json().catch(() => null)

  if (!response.ok) {
    const message =
      datos?.message || `Error ${response.status}: no se pudo completar la operación`
    throw new ApiError(response.status, message)
  }

  return datos
}

// ---- Autenticación -------------------------------------------------------

export const authApi = {
  login: (email, password) => request('POST', '/auth/login', { email, password }, { auth: false }),
  register: (data) => request('POST', '/auth/register', data, { auth: false }),
  crearUsuario: (data) => request('POST', '/auth/usuarios', data),
  me: () => request('GET', '/auth/me'),
}

// ---- Viajes ---------------------------------------------------------------

export const viajesApi = {
  listar: () => request('GET', '/viajes', undefined, { auth: false }),
  crear: (viaje) => request('POST', '/viajes', viaje),
  actualizar: (id, viaje) => request('PUT', `/viajes/${id}`, viaje),
  eliminar: (id) => request('DELETE', `/viajes/${id}`),
  descuentoLunaMiel: (id, descuento) =>
    request('PATCH', `/viajes/${id}/descuento-luna-miel?descuento=${descuento}`),
}

// ---- Actividades ----------------------------------------------------------

export const actividadesApi = {
  listar: () => request('GET', '/actividades', undefined, { auth: false }),
  hoy: (fecha) =>
    request(
      'GET',
      `/actividades/hoy${fecha ? `?fecha=${fecha}` : ''}`,
      undefined,
      { auth: false },
    ),
  crear: (actividad) => request('POST', '/actividades', actividad),
  actualizar: (id, actividad) => request('PUT', `/actividades/${id}`, actividad),
  eliminar: (id) => request('DELETE', `/actividades/${id}`),
  cambiarEstado: (id, estado) =>
    request('PATCH', `/actividades/${id}/estado?estado=${estado}`),
}

// ---- Clientes -------------------------------------------------------------

export const clientesApi = {
  crear: (cliente) => request('POST', '/clientes', cliente),
  obtener: (id) => request('GET', `/clientes/${id}`),
  obtenerPorDni: (dni) => request('GET', `/clientes/dni/${dni}`),
  actualizar: (id, cliente) => request('PUT', `/clientes/${id}`, cliente),
  eliminar: (id) => request('DELETE', `/clientes/${id}`),
}

// ---- Compras --------------------------------------------------------------

export const comprasApi = {
  listar: () => request('GET', '/compras'),
  obtener: (id) => request('GET', `/compras/${id}`),
  realizar: (compra) => request('POST', '/compras', compra),
  misCompras: () => request('GET', '/compras/mias'),
  realizarOnline: (compra) => request('POST', '/compras/mias', compra),
}

// ---- Inscripciones --------------------------------------------------------

export const inscripcionesApi = {
  inscribir: (clienteId, actividadId) => request('POST', '/inscripciones', { clienteId, actividadId }),
  listarPorActividad: (actividadId) => request('GET', `/inscripciones?actividadId=${actividadId}`),
  listarPorCliente: (clienteId) => request('GET', `/inscripciones?clienteId=${clienteId}`),
  anular: (id) => request('DELETE', `/inscripciones/${id}`),
  misInscripciones: () => request('GET', '/inscripciones/mias'),
  inscribirMia: (actividadId) => request('POST', '/inscripciones/mias', { actividadId }),
  anularMia: (id) => request('DELETE', `/inscripciones/mias/${id}`),
}

// ---- Tiendas --------------------------------------------------------------

export const tiendasApi = {
  listar: () => request('GET', '/tiendas'),
  obtener: (id) => request('GET', `/tiendas/${id}`),
  crear: (tienda) => request('POST', '/tiendas', tienda),
  actualizar: (id, tienda) => request('PUT', `/tiendas/${id}`, tienda),
  eliminar: (id) => request('DELETE', `/tiendas/${id}`),
}

// ---- Empleados ------------------------------------------------------------

export const empleadosApi = {
  listar: () => request('GET', '/empleados'),
  obtener: (id) => request('GET', `/empleados/${id}`),
  crear: (empleado) => request('POST', '/empleados', empleado),
  actualizar: (id, empleado) => request('PUT', `/empleados/${id}`, empleado),
  eliminar: (id) => request('DELETE', `/empleados/${id}`),
}

// ---- Utilidades de formato ------------------------------------------------

export function formatoMoneda(valor) {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(Number(valor ?? 0))
}

export function formatoFecha(fecha) {
  if (!fecha) return '—'
  return new Date(fecha).toLocaleDateString('es-ES')
}

export function formatoFechaHora(fechaHora) {
  if (!fechaHora) return '—'
  return new Date(fechaHora).toLocaleString('es-ES')
}