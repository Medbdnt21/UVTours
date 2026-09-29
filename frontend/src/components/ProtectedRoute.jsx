import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { Spinner } from './ui.jsx'

export default function ProtectedRoute({ children, roles = [] }) {
  const { usuario, cargando } = useAuth()
  const location = useLocation()

  if (cargando) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12">
        <Spinner />
      </div>
    )
  }

  if (!usuario) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (roles.length > 0 && !roles.includes(usuario.rol)) {
    return <Navigate to="/" replace />
  }

  return children
}