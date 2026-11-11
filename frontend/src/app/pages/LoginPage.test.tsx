import { describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { LoginPage } from './LoginPage';

function renderLogin() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
}

describe('LoginPage — validation (zod 4 + resolver)', () => {
  it('affiche les erreurs de champs obligatoires', async () => {
    renderLogin();

    fireEvent.click(screen.getByRole('button', { name: /se connecter/i }));

    expect(await screen.findByText("L'email est requis")).toBeInTheDocument();
    expect(screen.getByText('Le mot de passe est requis')).toBeInTheDocument();
  });

  it('rejette un email mal formé', async () => {
    renderLogin();

    fireEvent.change(screen.getByLabelText('Email professionnel'), {
      target: { value: 'pas-un-email' },
    });
    fireEvent.change(screen.getByLabelText('Mot de passe'), {
      target: { value: 'secret' },
    });
    fireEvent.click(screen.getByRole('button', { name: /se connecter/i }));

    expect(await screen.findByText("Format d'email invalide")).toBeInTheDocument();
  });
});
