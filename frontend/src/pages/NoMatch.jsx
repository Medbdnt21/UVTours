import { Link } from 'react-router-dom'

export default function NoMatch() {
  return (
    <div className="mx-auto max-w-md py-20 text-center">
      <p className="text-6xl font-bold text-slate-200">404</p>
      <h1 className="mt-4 text-xl font-bold text-slate-900">Página no encontrada</h1>
      <p className="mt-2 text-sm text-slate-500">
        La ruta que buscas no existe o no tienes permiso para verla.
      </p>
      <Link
        to="/"
        className="mt-6 inline-block rounded-lg bg-sky-600 px-4 py-2 text-sm font-medium text-white hover:bg-sky-700"
      >
        Volver al inicio
      </Link>
    </div>
  )
}