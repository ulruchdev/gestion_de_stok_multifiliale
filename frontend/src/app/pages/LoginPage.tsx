import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Mail, Lock, Eye, EyeOff, LogIn } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Card, CardContent } from '@/shared/ui/Card';
import { toast } from '@/shared/ui/Toast';
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
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 p-6">
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

          <div className="space-y-1">
            <Input
              label="Mot de passe"
              type={showPassword ? 'text' : 'password'}
              placeholder="Votre mot de passe"
              icon={<Lock className="h-4 w-4" />}
              error={errors.motDePasse?.message}
              autoComplete="current-password"
              disabled={isSubmitting}
              {...register('motDePasse')}
            />
            <div className="flex items-center justify-between">
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="text-xs text-[var(--brand-ink-muted)] hover:text-[var(--brand-primary)] transition-colors flex items-center gap-1"
              >
                {showPassword ? (
                  <><EyeOff className="h-3 w-3" /> Masquer</>
                ) : (
                  <><Eye className="h-3 w-3" /> Afficher</>
                )}
              </button>
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

      {/* Comptes de démonstration (dev only) */}
      {import.meta.env.DEV && (
        <Card variant="bordered" className="border-dashed">
          <CardContent padding="sm">
            <p className="text-xs font-medium text-[var(--brand-ink-muted)] mb-2">
              🧪 Comptes de démonstration (mode développement)
            </p>
            <div className="space-y-1 text-xs text-[var(--brand-ink-muted)]">
              <p><strong>Admin Groupe :</strong> admin@groupe.test / Test1234!</p>
              <p><strong>Admin Filiale :</strong> admin@filiale.test / Test1234!</p>
              <p><strong>Caissier :</strong> caissier@filiale.test / Test1234!</p>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
