'use client'

import { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import Image from 'next/image'
import { Camera, Loader2, Trash2 } from 'lucide-react'
import { updateProfileSchema, type UpdateProfileFormData } from '@/lib/validations/profile'
import { useProfile } from '@/hooks/useProfile'
import { createClient } from '@/lib/supabase/client'

const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
const MAX_ORIGINAL_SIZE_MB = 10
const MAX_ORIGINAL_SIZE_BYTES = MAX_ORIGINAL_SIZE_MB * 1024 * 1024
const AVATAR_MAX_DIMENSION = 512
const COMPRESS_QUALITY = 0.8

/**
 * Comprime e redimensiona uma imagem usando Canvas API.
 * Retorna um Blob JPEG otimizado (máximo 512×512px, qualidade 80%).
 */
function compressImage(file: File): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const img = new window.Image()
    img.onload = () => {
      let { width, height } = img

      // Redimensiona mantendo proporção
      if (width > AVATAR_MAX_DIMENSION || height > AVATAR_MAX_DIMENSION) {
        if (width > height) {
          height = Math.round((height * AVATAR_MAX_DIMENSION) / width)
          width = AVATAR_MAX_DIMENSION
        } else {
          width = Math.round((width * AVATAR_MAX_DIMENSION) / height)
          height = AVATAR_MAX_DIMENSION
        }
      }

      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height

      const ctx = canvas.getContext('2d')
      if (!ctx) {
        reject(new Error('Não foi possível criar contexto Canvas.'))
        return
      }

      ctx.drawImage(img, 0, 0, width, height)
      canvas.toBlob(
        (blob) => {
          if (blob) {
            resolve(blob)
          } else {
            reject(new Error('Falha ao comprimir imagem.'))
          }
        },
        'image/jpeg',
        COMPRESS_QUALITY
      )
    }
    img.onerror = () => reject(new Error('Falha ao carregar imagem.'))
    img.src = URL.createObjectURL(file)
  })
}

/**
 * Formulário de edição do perfil do usuário.
 * Usa React Hook Form + Zod para validação e useProfile para fetch/mutation.
 * Permite upload de avatar via Supabase Storage.
 */
export default function ProfileForm() {
  const { user, isLoading, isError, error, updateProfile, isUpdating } = useProfile()
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [apiError, setApiError] = useState<string | null>(null)
  const [isUploading, setIsUploading] = useState(false)
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isDirty },
  } = useForm<UpdateProfileFormData>({
    resolver: zodResolver(updateProfileSchema),
    defaultValues: {
      fullName: '',
      avatarUrl: '',
    },
  })

  // Sincroniza o formulário com os dados carregados do perfil
  useEffect(() => {
    if (user) {
      reset({
        fullName: user.fullName,
        avatarUrl: user.avatarUrl ?? '',
      })
      setAvatarPreview(user.avatarUrl)
    }
  }, [user, reset])

  /**
   * Comprime a imagem selecionada via Canvas e faz upload para o Supabase Storage.
   * Aceita originais de até 10MB — a compressão reduz para ~50-150KB (JPEG 512px).
   */
  async function handleAvatarUpload(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) return

    // Validação de tipo
    if (!ACCEPTED_IMAGE_TYPES.includes(file.type)) {
      setApiError('Formato de imagem não suportado. Use JPEG, PNG, WebP ou GIF.')
      return
    }

    // Validação de tamanho do original
    if (file.size > MAX_ORIGINAL_SIZE_BYTES) {
      setApiError(`A imagem original deve ter no máximo ${MAX_ORIGINAL_SIZE_MB}MB.`)
      return
    }

    setIsUploading(true)
    setApiError(null)

    try {
      // Comprime antes do upload
      const compressedBlob = await compressImage(file)

      const supabase = createClient()
      const fileName = `${user?.id}-${Date.now()}.jpg`
      const filePath = `avatars/${fileName}`

      const { error: uploadError } = await supabase.storage
        .from('avatars')
        .upload(filePath, compressedBlob, {
          upsert: true,
          contentType: 'image/jpeg',
        })

      if (uploadError) {
        throw new Error(uploadError.message)
      }

      const { data: publicUrlData } = supabase.storage
        .from('avatars')
        .getPublicUrl(filePath)

      const publicUrl = publicUrlData.publicUrl
      setAvatarPreview(publicUrl)
      setValue('avatarUrl', publicUrl, { shouldDirty: true })
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Erro ao fazer upload da imagem.')
    } finally {
      setIsUploading(false)
      // Limpa o input para permitir reselecionar o mesmo arquivo
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  /** Remove o avatar atual, voltando para a inicial do nome. */
  function handleRemoveAvatar() {
    setAvatarPreview(null)
    setValue('avatarUrl', '', { shouldDirty: true })
  }

  async function onSubmit(data: UpdateProfileFormData) {
    setSuccessMessage(null)
    setApiError(null)

    try {
      await updateProfile({
        fullName: data.fullName,
        avatarUrl: data.avatarUrl || null,
      })
      setSuccessMessage('Perfil atualizado com sucesso.')
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Erro ao atualizar perfil.')
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
      </div>
    )
  }

  if (isError) {
    return (
      <div className="rounded-md bg-red-50 p-4">
        <p className="text-sm text-red-700">
          Erro ao carregar perfil: {error?.message ?? 'Erro desconhecido'}
        </p>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Campo oculto para o avatarUrl — necessário para o RHF incluir no submit */}
      <input type="hidden" {...register('avatarUrl')} />

      {/* Avatar com upload */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-3">
          Foto de perfil
        </label>
        <div className="flex items-center gap-5">
          {/* Avatar clicável */}
          <div className="relative group">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading || isUpdating}
              className="relative h-20 w-20 shrink-0 overflow-hidden rounded-full bg-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
            >
              {avatarPreview ? (
                <Image
                  src={avatarPreview}
                  alt={user?.fullName ?? 'Avatar'}
                  width={80}
                  height={80}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center text-2xl font-semibold text-gray-500">
                  {user?.fullName?.charAt(0)?.toUpperCase() ?? '?'}
                </div>
              )}

              {/* Overlay ao hover */}
              <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100">
                {isUploading ? (
                  <Loader2 className="h-6 w-6 animate-spin text-white" />
                ) : (
                  <Camera className="h-6 w-6 text-white" />
                )}
              </div>
            </button>

            {/* Input de arquivo oculto */}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              onChange={handleAvatarUpload}
              className="hidden"
              aria-label="Selecionar imagem de perfil"
            />
          </div>

          {/* Info + ações */}
          <div className="space-y-1">
            <p className="text-sm font-medium text-gray-900">{user?.fullName}</p>
            <p className="text-sm text-gray-500">{user?.email}</p>
            <div className="flex items-center gap-3 pt-1">
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={isUploading || isUpdating}
                className="text-sm font-medium text-blue-600 hover:text-blue-700 disabled:opacity-50"
              >
                {isUploading ? 'Enviando...' : 'Alterar foto'}
              </button>
              {avatarPreview && (
                <button
                  type="button"
                  onClick={handleRemoveAvatar}
                  disabled={isUploading || isUpdating}
                  className="flex items-center gap-1 text-sm text-red-500 hover:text-red-600 disabled:opacity-50"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                  Remover
                </button>
              )}
            </div>
            <p className="text-xs text-gray-400">
              JPEG, PNG, WebP ou GIF. Máximo {MAX_ORIGINAL_SIZE_MB}MB (comprimido automaticamente).
            </p>
          </div>
        </div>
      </div>

      {/* Nome completo */}
      <div>
        <label htmlFor="fullName" className="block text-sm font-medium text-gray-700">
          Nome completo
        </label>
        <input
          id="fullName"
          type="text"
          {...register('fullName')}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
          disabled={isUpdating}
        />
        {errors.fullName && (
          <p className="mt-1 text-sm text-red-600">{errors.fullName.message}</p>
        )}
      </div>

      {/* E-mail (somente leitura — vem do Supabase Auth) */}
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700">
          E-mail
        </label>
        <input
          id="email"
          type="email"
          value={user?.email ?? ''}
          disabled
          className="mt-1 block w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-500"
        />
        <p className="mt-1 text-xs text-gray-500">
          O e-mail é gerenciado pelo provedor de autenticação e não pode ser alterado aqui.
        </p>
      </div>

      {/* Mensagens de feedback */}
      {successMessage && (
        <div className="rounded-md bg-green-50 p-3">
          <p className="text-sm text-green-700">{successMessage}</p>
        </div>
      )}

      {apiError && (
        <div className="rounded-md bg-red-50 p-3">
          <p className="text-sm text-red-700">{apiError}</p>
        </div>
      )}

      {/* Botão de submit */}
      <div className="flex justify-end">
        <button
          type="submit"
          disabled={isUpdating || !isDirty}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isUpdating ? 'Salvando...' : 'Salvar alterações'}
        </button>
      </div>
    </form>
  )
}
