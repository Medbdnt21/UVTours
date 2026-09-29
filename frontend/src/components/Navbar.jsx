import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const enlaceClase = ({ isActive }) =>
  `rounded-lg px-3 py-2 text-sm font-medium transition ${
    isActive ? 'bg-sky-100 text-sky-700' : 'text-slate-600 hover:bg-slate-100'
  }`

export default function Navbar() {
  const { usuario, cerrarSesion, esStaff, esAdmin } = useAuth()
  const navigate = useNavigate()

  const salir = () => {
    cerrarSesion()
    navigate('/')
  }

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3">
        <Link to="/" className="flex items-center gap-2 text-lg font-bold text-sky-700">
          <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 6h18M3 12h18M3 18h18M6 3v18M12 3v18M18 3v18"
            />
          </svg>
          UvTours
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          <NavLink to="/" className={enlaceClase} end>
            Inicio
          </NavLink>
          {esStaff && (
            <>
              <NavLink to="/admin/viajes" className={enlaceClase}>
                Viajes
              </NavLink>
              <NavLink to="/admin/actividades" className={enlaceClase}>
                Actividades
              </NavLink>
              <NavLink to="/admin/clientes" className={enlaceClase}>
                Clientes
              </NavLink>
              <NavLink to="/admin/compras" className={enlaceClase}>
                Compras
              </NavLink>
              <NavLink to="/admin/inscripciones" className={enlaceClase}>
                Inscripciones
              </NavLink>
              <NavLink to="/admin/tiendas" className={enlaceClase}>
                Tiendas
              </NavLink>
              <NavLink to="/admin/empleados" className={enlaceClase}>
                Empleados
              </NavLink>
            </>
          )}
          {usuario?.rol === 'CLIENTE' && (
            <NavLink to="/mi-cuenta" className={enlaceClase}>
              Mi cuenta
            </NavLink>
          )}
        </nav>

        <div className="flex items-center gap-3">
          {usuario ? (
            <>
              <div className="hidden text-right sm:block">
                <p className="text-sm font-medium text-slate-800">{usuario.nombre}</p>
                <p className="text-xs text-slate-500">{usuario.rol.toLowerCase()}</p>
              </div>
              <button
                onClick={salir}
                className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
              >
                Salir
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
              >
                Entrar
              </Link>
              <Link
                to="/register"
                className="rounded-lg bg-sky-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-sky-700"
              >
                Registrarse
              </Link>
            </>
          )}
        </div>
      </div>

      <nav className="flex gap-1 overflow-x-auto px-4 pb-2 md:hidden">
        <NavLink to="/" className={enlaceClase} end>
          Inicio
        </NavLink>
        {esStaff && (
          <>
            <NavLink to="/admin/viajes" className={enlaceClase}>Viajes</NavLink>
            <NavLink to="/admin/actividades" className={enlaceClase}>Actividades</NavLink>
            <NavLink to="/admin/clientes" className={enlaceClase}>Clientes</NavLink>
            <NavLink to="/admin/compras" className={enlaceClase}>Compras</NavLink>
            <NavLink to="/admin/inscripciones" className={enlaceClase}>Inscripciones</NavLink>
            <NavLink to="/admin/tiendas" className={enlaceClase}>Tiendas</NavLink>
            <NavLink to="/admin/empleados" className={enlaceClase}>Empleados</NavLink>
          </>
        )}
        {usuario?.rol === 'CLIENTE' && (
          <NavLink to="/mi-cuenta" className={enlaceClase}>Mi cuenta</NavLink>
        )}
      </nav>
    </header>
  )
}