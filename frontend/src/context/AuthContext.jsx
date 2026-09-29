import { createContext, useContext, useEffect, useState } from 'react'
import { authApi, getToken, setToken } from '../api/client.js'

const AuthContext = createContext(null)

const USUARIO_KEY = 'uvtours_usuario'

function guardarUsuario(usuario) {
  if (usuario) localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario))
  else localStorage.removeItem(USUARIO_KEY)
}

function leerUsuario() {
  try {
    return JSON.parse(localStorage.getItem(USUARIO_KEY))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerUsuario)
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    const validarSesion = async () => {
      if (!getToken()) {
        setCargando(false)
        return
      }
      try {
        const actual = await authApi.me()
        guardarUsuario(actual)
        setUsuario(actual)
      } catch {
        cerrarSesion()
      } finally {
        setCargando(false)
      }
    }
    validarSesion()
  }, [])

  const iniciarSesion = async (email, password) => {
    const respuesta = await authApi.login(email, password)
    setToken(respuesta.token)
    guardarUsuario(respuesta)
    setUsuario(respuesta)
    return respuesta
  }

  const registrar = async (data) => {
    const respuesta = await authApi.register(data)
    setToken(respuesta.token)
    guardarUsuario(respuesta)
    setUsuario(respuesta)
    return respuesta
  }

  const cerrarSesion = () => {
    setToken(null)
    guardarUsuario(null)
    setUsuario(null)
  }

  const esAdmin = usuario?.rol === 'ADMIN'
  const esEmpleado = usuario?.rol === 'EMPLEADO'
  const esCliente = usuario?.rol === 'CLIENTE'
  const esStaff = esAdmin || esEmpleado

  return (
    <AuthContext.Provider
      value={{ usuario, cargando, iniciarSesion, registrar, cerrarSesion, esAdmin, esEmpleado, esCliente, esStaff }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}