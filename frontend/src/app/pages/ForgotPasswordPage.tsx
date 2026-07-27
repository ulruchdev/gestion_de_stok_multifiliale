import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Mail, ArrowLeft, Send, CheckCircle2 } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Card } from '@/shared/ui/Card';
import { toast } from '@/shared/ui';
import { apiClient } from '@/shared/lib/api-client';

const forgotPasswordSchema = z.object({
  email: z
    .string()
    .min(1, "L'email est requis")
    .email("Format d'email invalide"),
});

type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>;

export function ForgotPasswordPage() {
  const [sent, setSent] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
    mode: 'onChange',
    defaultValues: { email: '' },
  });

  const onSubmit = async (data: ForgotPasswordFormData) => {
    try {
      await apiClient.post('/auth/forgot-password', { email: data.email });
      setSent(true);
      toast.success('Email envoyé', 'Vérifiez votre boîte de réception');
    } catch (error) {
      // Ne pas révéler si l'email existe ou pas (sécurité)
      setSent(true);
    }
  };

  if (sent) {
    return (
      <div className="space-y-6 animate-fade-in-up text-center">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-[var(--stock-success-bg)]">
          <CheckCircle2 className="h-7 w-7 text-[var(--stock-success)]" />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-semibold font-display tracking-tight text-[var(--brand-ink)]">
            Email envoyé
          </h1>
          <p className="text-sm text-[var(--brand-ink-muted)]">
            Si un compte existe avec cette adresse, vous recevrez un lien de réinitialisation sous quelques minutes.
          </p>
        </div>
        <Link
          to="/login"
          className="inline-flex items-center gap-2 text-sm font-medium text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
        >
          <ArrowLeft className="h-4 w-4" /> Retour à la connexion
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in-up">
      {/* En-tête */}
      <div className="text-center space-y-2">
        <h1 className="text-2xl font-semibold font-display tracking-tight text-[var(--brand-ink)]">
          Mot de passe oublié
        </h1>
        <p className="text-sm text-[var(--brand-ink-muted)]">
          Saisissez votre email pour recevoir un lien de réinitialisation
        </p>
      </div>

      {/* Formulaire */}
      <Card variant="elevated">
        <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-5">
          <Input
            label="Email professionnel"
            type="email"
            placeholder="exemple@entreprise.com"
            icon={<Mail className="h-4 w-4" />}
            error={errors.email?.message}
            autoComplete="email"
            disabled={isSubmitting}
            {...register('email')}
          />

          <Button type="submit" className="w-full" size="lg" loading={isSubmitting}>
            <Send className="h-4 w-4" />
            Envoyer le lien
          </Button>
        </form>
      </Card>

      {/* Lien retour */}
      <p className="text-center text-sm text-[var(--brand-ink-muted)]">
        <Link
          to="/login"
          className="font-medium text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors inline-flex items-center gap-1"
        >
          <ArrowLeft className="h-4 w-4" /> Retour à la connexion
        </Link>
      </p>
    </div>
  );
}
