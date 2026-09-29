import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import MiCuenta from './pages/MiCuenta.jsx'
import ViajesAdmin from './pages/admin/ViajesAdmin.jsx'
import ActividadesAdmin from './pages/admin/ActividadesAdmin.jsx'
import ClientesAdmin from './pages/admin/ClientesAdmin.jsx'
import ComprasAdmin from './pages/admin/ComprasAdmin.jsx'
import InscripcionesAdmin from './pages/admin/InscripcionesAdmin.jsx'
import TiendasAdmin from './pages/admin/TiendasAdmin.jsx'
import EmpleadosAdmin from './pages/admin/EmpleadosAdmin.jsx'
import NoMatch from './pages/NoMatch.jsx'

export default function App() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route
            path="/mi-cuenta"
            element={
              <ProtectedRoute roles={['CLIENTE']}>
                <MiCuenta />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/viajes"
            element={
              <ProtectedRoute roles={['ADMIN']}>
                <ViajesAdmin />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/actividades"
            element={
              <ProtectedRoute roles={['ADMIN', 'EMPLEADO']}>
                <ActividadesAdmin />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/clientes"
            element={
              <ProtectedRoute roles={['ADMIN', 'EMPLEADO']}>
                <ClientesAdmin />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/compras"
            element={
              <ProtectedRoute roles={['ADMIN', 'EMPLEADO']}>
                <ComprasAdmin />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/inscripciones"
            element={
              <ProtectedRoute roles={['ADMIN', 'EMPLEADO']}>
                <InscripcionesAdmin />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/tiendas"
            element={
              <ProtectedRoute roles={['ADMIN', 'EMPLEADO']}>
                <TiendasAdmin />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/empleados"
            element={
              <ProtectedRoute roles={['ADMIN', 'EMPLEADO']}>
                <EmpleadosAdmin />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<NoMatch />} />
        </Routes>
      </main>
      <footer className="border-t border-slate-200 bg-white py-4 text-center text-xs text-slate-400">
        UvTours · Agencia de viajes · {new Date().getFullYear()}
      </footer>
    </div>
  )
}