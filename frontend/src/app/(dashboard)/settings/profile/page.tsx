import ProfileForm from '@/components/profile/ProfileForm'

export default function ProfileSettingsPage() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Perfil</h1>
        <p className="mt-2 text-sm text-gray-600">
          Gerencie seu nome, avatar e informações pessoais.
        </p>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <ProfileForm />
      </div>
    </div>
  )
}
