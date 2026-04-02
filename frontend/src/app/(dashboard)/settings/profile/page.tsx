import ProfileForm from '@/components/profile/ProfileForm'

export default function ProfileSettingsPage() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Perfil</h1>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
          Gerencie seu nome, avatar e informações pessoais.
        </p>
      </div>

      <div className="card">
        <ProfileForm />
      </div>
    </div>
  )
}
