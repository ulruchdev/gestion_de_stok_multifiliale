import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { Input } from './Input';

describe('Input — accessibilité', () => {
  it('rend le bouton d’icône atteignable au clavier avec un nom accessible', () => {
    const onToggle = vi.fn();
    render(
      <Input
        label="Mot de passe"
        type="password"
        trailingIcon={<span>œil</span>}
        onTrailingIconClick={onToggle}
      />,
    );

    const toggle = screen.getByRole('button', { name: 'Afficher ou masquer le mot de passe' });
    expect(toggle).toHaveAttribute('type', 'button');
    expect(toggle).not.toHaveAttribute('tabindex', '-1');
    toggle.focus();
    expect(document.activeElement).toBe(toggle);

    fireEvent.click(toggle);
    expect(onToggle).toHaveBeenCalledTimes(1);
  });

  it('relie description et erreur au champ', () => {
    const { rerender } = render(
      <Input label="Email" description="Utilisé pour la connexion" />,
    );
    const input = screen.getByLabelText('Email');
    expect(input).toHaveAccessibleDescription('Utilisé pour la connexion');
    expect(input).not.toHaveAttribute('aria-invalid', 'true');

    rerender(<Input label="Email" description="Utilisé pour la connexion" error="Email requis" />);
    expect(screen.getByLabelText('Email')).toHaveAccessibleDescription('Email requis');
    expect(screen.getByLabelText('Email')).toHaveAttribute('aria-invalid', 'true');
  });
});
