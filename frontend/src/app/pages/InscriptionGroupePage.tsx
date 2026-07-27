import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Building2,
  ArrowLeft,
  ArrowRight,
  Check,
  Eye,
  EyeOff,
  Mail,
  Phone,
  User,
  Lock,
  FileText,
  Globe,
} from 'lucide-react';
import { Logo } from '@/shared/ui/Logo';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { PhoneInputField } from '@/shared/ui/PhoneInput';
import { Card, CardContent } from '@/shared/ui/Card';
import { toast } from '@/shared/ui';
import { apiClient, getErrorMessage } from '@/shared/lib/api-client';
import { cn } from '@/shared/lib/utils';

const passwordSchema = z
  .string()
  .min(8, 'Minimum 8 caractères')
  .max(50, 'Maximum 50 caractères')
  .regex(/[A-Z]/, 'Au moins une majuscule')
  .regex(/[a-z]/, 'Au moins une minuscule')
  .regex(/[0-9]/, 'Au moins un chiffre')
  .regex(/[!@#$%^&*()_+={}[\]|:;<>,.?/~`-]/, 'Au moins un caractère spécial');

const inscriptionGroupeSchema = z.object({
  nomGroupe: z
    .string()
    .min(2, 'Le nom du groupe est requis')
    .max(100, 'Maximum 100 caractères'),
  nomEntreprise: z
    .string()
    .min(2, "Le nom de l'entreprise est requis")
    .max(100, 'Maximum 100 caractères'),
  nif: z
    .string()
    .min(1, 'Le NIF est requis')
    .max(20, 'Maximum 20 caractères'),
  email: z
    .string()
    .min(1, "L'email est requis")
    .email("Format d'email invalide"),
  telephone: z
    .string()
    .min(8, 'Numéro de téléphone invalide')
    .max(16, 'Maximum 16 caractères')
    .regex(/^\+[1-9]\d{1,14}$/, 'Format international invalide'),
  adminNom: z
    .string()
    .min(2, 'Le nom est requis')
    .max(50, 'Maximum 50 caractères'),
  adminPrenom: z
    .string()
    .min(2, 'Le prénom est requis')
    .max(50, 'Maximum 50 caractères'),
  adminMotDePasse: passwordSchema,
  confirmMotDePasse: z.string().min(1, 'Confirmation requise'),
}).refine((data) => data.adminMotDePasse === data.confirmMotDePasse, {
  message: 'Les mots de passe ne correspondent pas',
  path: ['confirmMotDePasse'],
});

type InscriptionGroupeFormData = z.infer<typeof inscriptionGroupeSchema>;

const steps = [
  { id: 'groupe', label: 'Groupe', icon: Globe },
  { id: 'entreprise', label: 'Filiale', icon: Building2 },
  { id: 'administrateur', label: 'Admin', icon: User },
  { id: 'confirmation', label: 'Confirmer', icon: Check },
];

const passwordChecks = [
  { label: '8 caractères minimum', test: (v: string) => v.length >= 8 },
  { label: 'Une majuscule', test: (v: string) => /[A-Z]/.test(v) },
  { label: 'Un chiffre', test: (v: string) => /[0-9]/.test(v) },
  { label: 'Un caractère spécial', test: (v: string) => /[!@#$%^&*()_+={}[\]|:;<>,.?/~`-]/.test(v) },
];

function PasswordStrengthChecklist({ password, checks }: { password: string; checks: Array<{ label: string; test: (v: string) => boolean }> }) {
  return (
    <div className="rounded-lg bg-[var(--brand-canvas-soft)] p-3">
      <p className="text-xs font-medium text-[var(--brand-ink-muted)] mb-2">
        Le mot de passe doit contenir :
      </p>
      <div className="space-y-1.5">
        {checks.map((check) => {
          const isValid = password ? check.test(password) : false;
          return (
            <div key={check.label} className="flex items-center gap-2 text-xs">
              <div
                className={`w-3.5 h-3.5 rounded-full flex items-center justify-center ${
                  isValid ? 'text-[var(--stock-success)]' : 'text-[var(--brand-ink-muted)]'
                }`}
              >
                {isValid ? (
                  <Check className="h-3 w-3" />
                ) : (
                  <div className="w-1.5 h-1.5 rounded-full bg-current" />
                )}
              </div>
              <span className={isValid ? 'text-[var(--stock-success)]' : 'text-[var(--brand-ink-muted)]'}>
                {check.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function InscriptionGroupePage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const {
    register,
    handleSubmit,
    trigger,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<InscriptionGroupeFormData>({
    resolver: zodResolver(inscriptionGroupeSchema),
    mode: 'onChange',
    defaultValues: {
      nomGroupe: '',
      nomEntreprise: '',
      nif: '',
      email: '',
      telephone: '',
      adminNom: '',
      adminPrenom: '',
      adminMotDePasse: '',
      confirmMotDePasse: '',
    },
  });

  const nextStep = async () => {
    const fieldsToValidate: (keyof InscriptionGroupeFormData)[] =
      step === 0
        ? ['nomGroupe']
        : step === 1
          ? ['nomEntreprise', 'nif', 'email', 'telephone']
          : ['adminNom', 'adminPrenom', 'adminMotDePasse', 'confirmMotDePasse'];

    const isValid = await trigger(fieldsToValidate);
    if (isValid) setStep((prev) => Math.min(prev + 1, steps.length - 1));
  };

  const prevStep = () => setStep((prev) => Math.max(prev - 1, 0));

  const onSubmit = async (data: InscriptionGroupeFormData) => {
    try {
      await apiClient.post('/auth/inscription/groupe', {
        nomGroupe: data.nomGroupe,
        nomEntreprise: data.nomEntreprise,
        nif: data.nif,
        email: data.email,
        telephone: data.telephone,
        adminNom: data.adminNom,
        adminPrenom: data.adminPrenom,
        adminMotDePasse: data.adminMotDePasse,
      });

      toast.success('Inscription réussie !', 'Votre groupe a été créé. Vous pouvez maintenant vous connecter.');
      navigate('/login');
    } catch (error) {
      const message = getErrorMessage(error);
      toast.error("Échec de l'inscription", message);
    }
  };  return (
    <div className="min-h-screen flex items-center justify-center py-8 px-4 sm:px-6">
      <div className="w-full max-w-2xl mx-auto space-y-6 animate-fade-in-up">
        {/* Logo centré */}
        <div className="text-center">
          <Logo variant="auth" size="md" />
        </div>

        {/* En-tête */}
        <div className="text-center space-y-2 mt-6">
          <h1 className="text-2xl font-semibold font-display tracking-tight text-[var(--brand-ink)]">
            Groupe multi-sites
          </h1>
          <p className="text-sm text-[var(--brand-ink-muted)]">
            Créez votre groupe et votre première filiale
          </p>
        </div>

        {/* Stepper */}
        <div className="flex items-center justify-center gap-2">
        {steps.map((s, i) => {
          const StepIcon = s.icon;
          const isActive = i === step;
          const isCompleted = i < step;

          return (
            <div key={s.id} className="flex items-center gap-2">
              <div
                className={cn(
                  'flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium transition-all duration-[var(--brand-duration-fast)]',
                  isActive && 'bg-[var(--brand-primary)] text-white',
                  isCompleted && 'bg-[var(--stock-success-bg)] text-[var(--stock-success)]',
                  !isActive && !isCompleted && 'bg-muted text-muted-foreground',
                )}
              >
                <StepIcon className="h-3.5 w-3.5" />
                <span className="hidden sm:inline">{s.label}</span>
              </div>
              {i < steps.length - 1 && (
                <div
                  className={cn(
                    'h-px w-6',
                    i < step ? 'bg-[var(--stock-success)]' : 'bg-border',
                  )}
                />
              )}
            </div>
          );
        })}
      </div>

      {/* Formulaire */}
      <Card variant="elevated">
        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent padding="lg" className="space-y-5">
            {/* Étape 1 : Informations du groupe */}
            {step === 0 && (
              <div className="space-y-4 animate-fade-in-up">
                <h2 className="text-base font-semibold font-display text-[var(--brand-ink)] flex items-center gap-2">
                  <Globe className="h-4 w-4 text-[var(--brand-primary)]" />
                  Informations du groupe
                </h2>
                <Input
                  label="Nom du groupe"
                  placeholder="Mon Groupe de Distribution"
                  icon={<Globe className="h-4 w-4" />}
                  error={errors.nomGroupe?.message}
                  disabled={isSubmitting}
                  {...register('nomGroupe')}
                />
              </div>
            )}

            {/* Étape 2 : Première filiale */}
            {step === 1 && (
              <div className="space-y-4 animate-fade-in-up">
                <h2 className="text-base font-semibold font-display text-[var(--brand-ink)] flex items-center gap-2">
                  <Building2 className="h-4 w-4 text-[var(--brand-primary)]" />
                  Première filiale
                </h2>
                <Input
                  label="Nom de la filiale"
                  placeholder="Ma Boutique Centre"
                  icon={<Building2 className="h-4 w-4" />}
                  error={errors.nomEntreprise?.message}
                  disabled={isSubmitting}
                  {...register('nomEntreprise')}
                />
                <Input
                  label="NIF (Numéro d'Identification Fiscale)"
                  placeholder="P0123456789012345"
                  icon={<FileText className="h-4 w-4" />}
                  error={errors.nif?.message}
                  disabled={isSubmitting}
                  {...register('nif')}
                />
                <div className="grid gap-4 sm:grid-cols-2">
                  <Input
                    label="Email professionnel"
                    type="email"
                    placeholder="contact@filiale.cm"
                    icon={<Mail className="h-4 w-4" />}
                    error={errors.email?.message}
                    disabled={isSubmitting}
                    {...register('email')}
                  />
                  <PhoneInputField
                    label="Téléphone"
                    placeholder="691234567"
                    icon={<Phone className="h-4 w-4" />}
                    error={errors.telephone?.message}
                    disabled={isSubmitting}
                    value={watch('telephone') || undefined}
                    onChange={(value) => setValue('telephone', value || '', { shouldValidate: true })}
                    defaultCountry="CM"
                  />
                </div>
              </div>
            )}

            {/* Étape 3 : Administrateur */}
            {step === 2 && (
              <div className="space-y-4 animate-fade-in-up">
                <h2 className="text-base font-semibold font-display text-[var(--brand-ink)] flex items-center gap-2">
                  <User className="h-4 w-4 text-[var(--brand-primary)]" />
                  Administrateur du groupe
                </h2>
                <div className="grid gap-4 sm:grid-cols-2">
                  <Input
                    label="Nom"
                    placeholder="Kamga"
                    icon={<User className="h-4 w-4" />}
                    error={errors.adminNom?.message}
                    disabled={isSubmitting}
                    {...register('adminNom')}
                  />
                  <Input
                    label="Prénom"
                    placeholder="Jean"
                    icon={<User className="h-4 w-4" />}
                    error={errors.adminPrenom?.message}
                    disabled={isSubmitting}
                    {...register('adminPrenom')}
                  />
                </div>
                <Input
                  label="Mot de passe"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Créez un mot de passe sécurisé"
                  icon={<Lock className="h-4 w-4" />}
                  trailingIcon={showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  onTrailingIconClick={() => setShowPassword(!showPassword)}
                  error={errors.adminMotDePasse?.message}
                  autoComplete="new-password"
                  disabled={isSubmitting}
                  {...register('adminMotDePasse')}
                />
                <Input
                  label="Confirmer le mot de passe"
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Ressaisissez le mot de passe"
                  icon={<Lock className="h-4 w-4" />}
                  trailingIcon={showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  onTrailingIconClick={() => setShowConfirm(!showConfirm)}
                  error={errors.confirmMotDePasse?.message}
                  autoComplete="new-password"
                  disabled={isSubmitting}
                  {...register('confirmMotDePasse')}
                />
                <PasswordStrengthChecklist
                  password={watch('adminMotDePasse') || ''}
                  checks={passwordChecks}
                />
              </div>
            )}

            {/* Étape 4 : Résumé */}
            {step === 3 && (
              <div className="space-y-4 animate-fade-in-up">
                <h2 className="text-base font-semibold font-display text-[var(--brand-ink)] flex items-center gap-2">
                  <Check className="h-4 w-4 text-[var(--stock-success)]" />
                  Vérification avant validation
                </h2>
                <p className="text-sm text-[var(--brand-ink-muted)]">
                  Vérifiez les informations ci-dessous avant de créer votre groupe.
                </p>
                <div className="rounded-lg border border-[var(--brand-hairline)] divide-y divide-[var(--brand-hairline)]">
                  <div className="p-3 flex items-center justify-between text-sm">
                    <span className="text-[var(--brand-ink-muted)]">Groupe</span>
                    <span className="font-medium text-[var(--brand-ink)]">{watch('nomGroupe')}</span>
                  </div>
                  <div className="p-3 flex items-center justify-between text-sm">
                    <span className="text-[var(--brand-ink-muted)]">Filiale</span>
                    <span className="font-medium text-[var(--brand-ink)]">{watch('nomEntreprise')}</span>
                  </div>
                  <div className="p-3 flex items-center justify-between text-sm">
                    <span className="text-[var(--brand-ink-muted)]">NIF</span>
                    <span className="font-medium text-[var(--brand-ink)]">{watch('nif')}</span>
                  </div>
                  <div className="p-3 flex items-center justify-between text-sm">
                    <span className="text-[var(--brand-ink-muted)]">Email</span>
                    <span className="font-medium text-[var(--brand-ink)]">{watch('email')}</span>
                  </div>
                  <div className="p-3 flex items-center justify-between text-sm">
                    <span className="text-[var(--brand-ink-muted)]">Téléphone</span>
                    <span className="font-medium text-[var(--brand-ink)]">{watch('telephone')}</span>
                  </div>
                  <div className="p-3 flex items-center justify-between text-sm">
                    <span className="text-[var(--brand-ink-muted)]">Administrateur</span>
                    <span className="font-medium text-[var(--brand-ink)]">{watch('adminPrenom')} {watch('adminNom')}</span>
                  </div>
                </div>
              </div>
            )}

            {/* Navigation */}
            <div className="flex items-center justify-between pt-2 border-t border-[var(--brand-hairline)]">
              {step > 0 ? (
                <Button type="button" variant="outline" onClick={prevStep} disabled={isSubmitting}>
                  <ArrowLeft className="h-4 w-4" /> Retour
                </Button>
              ) : (
                <Link
                  to="/inscription"
                  className="inline-flex items-center gap-1 text-sm text-[var(--brand-ink-muted)] hover:text-[var(--brand-primary)] transition-colors"
                >
                  <ArrowLeft className="h-4 w-4" /> Changer de type
                </Link>
              )}

              {step < steps.length - 1 ? (
                <Button type="button" onClick={nextStep}>
                  Suivant <ArrowRight className="h-4 w-4" />
                </Button>
              ) : (
                <Button type="submit" variant="brand" loading={isSubmitting} size="lg">
                  <Check className="h-4 w-4" /> Créer mon groupe
                </Button>
              )}
            </div>
          </CardContent>
        </form>
      </Card>

        {/* Lien connexion */}
        <p className="text-center text-sm text-[var(--brand-ink-muted)]">
          Déjà un compte ?{' '}
          <Link
            to="/login"
            className="font-semibold text-[var(--brand-primary)] hover:text-[var(--brand-primary-hover)] transition-colors"
          >
            Se connecter
          </Link>
        </p>
      </div>
    </div>
  );
}
