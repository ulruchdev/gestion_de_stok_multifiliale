import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Mail, Lock, Eye, EyeOff, LogIn } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Card } from '@/shared/ui/Card';
import { toast } from '@/shared/ui';
import { useAuthStore } from '@/features/auth';
import { getErrorMessage } from '@/shared/lib/api-client';

const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'L\'email est requis')
    .email('Format d\'email invalide'),
  motDePasse: z
    .string()
    .min(1, 'Le mot de passe est requis'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginPage() {
  const navigate = useNavigate();
  const login = useAuthStore((s) => s.login);
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: 'onChange',
    defaultValues: {
      email: '',
      motDePasse: '',
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      await login({
        email: data.email,
        motDePasse: data.motDePasse,
      });
      toast.success('Connexion réussie', 'Bienvenue sur StockMaster CM');
      navigate('/dashboard');
    } catch (error) {
      const message = getErrorMessage(error);
      toast.error('Échec de la connexion', message);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in-up">
      {/* En-tête */}
      <div className="text-center space-y-2">
        <h1 className="text-2xl font-semibold font-display tracking-tight text-[var(--brand-ink)]">
          Connexion
        </h1>
        <p className="text-sm text-[var(--brand-ink-muted)]">
          Accédez à votre espace de gestion
        </p>
      </div>

      {/* Formulaire */}
      <Card variant="elevated">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 p-7">
          <Input
            label="Email professionnel"
            type="email"
            placeholder="exemple@entreprise.com"
            icon={<Mail className="h-4 w-4" />}
            error={errors.email?.message}
            autoComplete="email"
            disabled={isSubmitting}
            inputSize="lg"
            {...register('email')}
          />

          <div className="space-y-1.5">
            <Input
              label="Mot de passe"
              type={showPassword ? 'text' : 'password'}
              placeholder="Votre mot de passe"
              icon={<Lock className="h-4 w-4" />}
              trailingIcon={showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              onTrailingIconClick={() => setShowPassword(!showPassword)}
              error={errors.motDePasse?.message}
              autoComplete="current-password"
              disabled={isSubmitting}
              inputSize="lg"
              {...register('motDePasse')}
            />
            <div className="flex justify-end">
              <Link
                to="/mot-de-passe-oublie"
                className="text-xs font-medium text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
              >
                Mot de passe oublié ?
              </Link>
            </div>
          </div>

          <Button
            type="submit"
            className="w-full"
            size="lg"
            loading={isSubmitting}
          >
            <LogIn className="h-4 w-4" />
            Se connecter
          </Button>
        </form>
      </Card>

      {/* Lien inscription */}
      <p className="text-center text-sm text-[var(--brand-ink-muted)]">
        Pas encore de compte ?{' '}
        <Link
          to="/inscription"
          className="font-semibold text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
        >
          Créer mon espace
        </Link>
      </p>


    </div>
  );
}
