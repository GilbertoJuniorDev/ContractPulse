import Link from 'next/link'

const settingsSections = [
  {
    title: 'Perfil',
    description: 'Gerencie seu nome, avatar e preferências pessoais.',
    href: '/settings/profile',
  },
  {
    title: 'Organizações',
    description: 'Crie e gerencie suas organizações para vincular contratos.',
    href: '/settings/organization',
  },
]

export default function SettingsPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900">Configurações</h1>
      <p className="mt-2 text-sm text-gray-600">
        Gerencie sua conta e preferências do sistema.
      </p>

      <div className="mt-8 grid gap-4 sm:grid-cols-2">
        {settingsSections.map((section) => (
          <Link
            key={section.href}
            href={section.href}
            className="group rounded-lg border border-gray-200 bg-white p-6 shadow-sm transition-shadow hover:shadow-md"
          >
            <h2 className="text-lg font-semibold text-gray-900 group-hover:text-blue-600">
              {section.title}
            </h2>
            <p className="mt-2 text-sm text-gray-500">{section.description}</p>
          </Link>
        ))}
      </div>
    </div>
  )
}
