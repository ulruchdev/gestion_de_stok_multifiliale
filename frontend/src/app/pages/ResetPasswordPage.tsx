import { useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Lock, Eye, EyeOff, CheckCircle2, AlertCircle } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Card, CardContent } from '@/shared/ui/Card';
import { toast } from '@/shared/ui/Toast';
import { apiClient, getErrorMessage } from '@/shared/lib/api-client';

const resetPasswordSchema = z
  .object({
    motDePasse: z
      .string()
      .min(8, 'Minimum 8 caractères')
      .max(50, 'Maximum 50 caractères')
      .regex(/[A-Z]/, 'Au moins une majuscule')
      .regex(/[a-z]/, 'Au moins une minuscule')
      .regex(/[0-9]/, 'Au moins un chiffre')
      .regex(/[!@#$%^&*()_+={}\[\]|:;<>,.?/~`-]/, 'Au moins un caractère spécial'),
    confirmMotDePasse: z.string().min(1, 'Confirmation requise'),
  })
  .refine((data) => data.motDePasse === data.confirmMotDePasse, {
    message: 'Les mots de passe ne correspondent pas',
    path: ['confirmMotDePasse'],
  });

type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      motDePasse: '',
      confirmMotDePasse: '',
    },
  });

  const onSubmit = async (data: ResetPasswordFormData) => {
    if (!token) {
      toast.error('Lien invalide', 'Le lien de réinitialisation est invalide ou expiré.');
      return;
    }

    try {
      await apiClient.post('/auth/reset-password', {
        token,
        motDePasse: data.motDePasse,
      });

      toast.success('Mot de passe réinitialisé', 'Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.');
      navigate('/login');
    } catch (error) {
      const message = getErrorMessage(error);
      toast.error('Échec de la réinitialisation', message);
    }
  };

  // Pas de token dans l'URL
  if (!token) {
    return (
      <div className="space-y-6 animate-fade-in-up text-center">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-[var(--stock-danger-bg)]">
          <AlertCircle className="h-7 w-7 text-[var(--stock-danger)]" />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-semibold font-display tracking-tight text-[var(--brand-ink)]">
            Lien invalide
          </h1>
          <p className="text-sm text-[var(--brand-ink-muted)]">
            Ce lien de réinitialisation est invalide ou a expiré. Veuillez refaire une demande.
          </p>
        </div>
        <Link
          to="/mot-de-passe-oublie"
          className="inline-flex items-center gap-2 text-sm font-medium text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
        >
          Retour
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in-up">
      {/* En-tête */}
      <div className="text-center space-y-2">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-[var(--brand-primary-bg)] mb-1">
          <Lock className="h-6 w-6 text-[var(--brand-primary)]" />
        </div>
        <h1 className="text-2xl font-semibold font-display tracking-tight text-[var(--brand-ink)]">
          Nouveau mot de passe
        </h1>
        <p className="text-sm text-[var(--brand-ink-muted)]">
          Choisissez un nouveau mot de passe sécurisé
        </p>
      </div>

      {/* Formulaire */}
      <Card variant="elevated">
        <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-5">
          <div className="space-y-1">
            <Input
              label="Nouveau mot de passe"
              type={showPassword ? 'text' : 'password'}
              placeholder="Minimum 8 caractères"
              icon={<Lock className="h-4 w-4" />}
              error={errors.motDePasse?.message}
              autoComplete="new-password"
              disabled={isSubmitting}
              {...register('motDePasse')}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="text-xs text-[var(--brand-ink-muted)] hover:text-[var(--brand-primary)] transition-colors flex items-center gap-1"
            >
              {showPassword ? <><EyeOff className="h-3 w-3" /> Masquer</> : <><Eye className="h-3 w-3" /> Afficher</>}
            </button>
          </div>

          <Input
            label="Confirmer le mot de passe"
            type={showConfirm ? 'text' : 'password'}
            placeholder="Ressaisissez le mot de passe"
            icon={<Lock className="h-4 w-4" />}
            error={errors.confirmMotDePasse?.message}
            autoComplete="new-password"
            disabled={isSubmitting}
            {...register('confirmMotDePasse')}
          />

          <Button type="submit" className="w-full" size="lg" loading={isSubmitting}>
            <CheckCircle2 className="h-4 w-4" />
            Réinitialiser le mot de passe
          </Button>
        </form>
      </Card>

      {/* Lien retour */}
      <p className="text-center text-sm text-[var(--brand-ink-muted)]">
        <Link
          to="/login"
          className="font-medium text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
        >
          Retour à la connexion
        </Link>
      </p>
    </div>
  );
}
