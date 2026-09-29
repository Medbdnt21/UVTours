import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { Button, Card, Input, Alert } from '../components/ui.jsx'

export default function Register() {
  const { registrar } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ nombre: '', email: '', dni: '', password: '' })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  const enviar = async (e) => {
    e.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      await registrar(form)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="mx-auto max-w-md">
      <Card className="p-8">
        <h1 className="mb-1 text-2xl font-bold text-slate-900">Crear cuenta</h1>
        <p className="mb-6 text-sm text-slate-500">
          Regístrate como cliente para comprar viajes online e inscribirte en actividades
        </p>

        {error && (
          <div className="mb-4">
            <Alert>{error}</Alert>
          </div>
        )}

        <form onSubmit={enviar} className="space-y-4">
          <Input
            label="Nombre"
            required
            value={form.nombre}
            onChange={cambiar('nombre')}
            placeholder="Tu nombre"
          />
          <Input
            label="Email"
            type="email"
            required
            autoComplete="email"
            value={form.email}
            onChange={cambiar('email')}
            placeholder="tucorreo@ejemplo.com"
          />
          <Input
            label="DNI (opcional)"
            value={form.dni}
            onChange={cambiar('dni')}
            placeholder="12345678A"
          />
          <Input
            label="Contraseña"
            type="password"
            required
            minLength={6}
            autoComplete="new-password"
            value={form.password}
            onChange={cambiar('password')}
            placeholder="Mínimo 6 caracteres"
          />
          <Button type="submit" disabled={enviando} className="w-full">
            {enviando ? 'Creando cuenta…' : 'Crear cuenta'}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="font-medium text-sky-600 hover:underline">
            Entrar
          </Link>
        </p>
      </Card>
    </div>
  )
}