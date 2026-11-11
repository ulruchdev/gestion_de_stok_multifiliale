import { useState } from 'react';
import { describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { Modal } from './Modal';

function Harness() {
  const [open, setOpen] = useState(false);
  return (
    <div>
      <button type="button" onClick={() => setOpen(true)}>
        Ouvrir
      </button>
      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title="Confirmer l’archivage"
        description="Cette action est réversible."
      >
        <button type="button">Premier</button>
        <button type="button">Second</button>
      </Modal>
    </div>
  );
}

describe('Modal — accessibilité', () => {
  it('place le focus dans la modale et expose titre et description', () => {
    render(<Harness />);
    fireEvent.click(screen.getByRole('button', { name: 'Ouvrir' }));

    const dialog = screen.getByRole('dialog');
    expect(dialog).toHaveAccessibleName('Confirmer l’archivage');
    expect(dialog).toHaveAccessibleDescription('Cette action est réversible.');
    expect(document.activeElement).toBe(screen.getByRole('button', { name: 'Fermer' }));
  });

  it('boucle le focus avec Tab au lieu de sortir de la modale', () => {
    render(<Harness />);
    fireEvent.click(screen.getByRole('button', { name: 'Ouvrir' }));

    const last = screen.getByRole('button', { name: 'Second' });
    const first = screen.getByRole('button', { name: 'Fermer' });
    last.focus();

    fireEvent.keyDown(document, { key: 'Tab' });

    expect(document.activeElement).toBe(first);
  });

  it('ferme avec Échap et rend le focus au déclencheur', () => {
    render(<Harness />);
    const trigger = screen.getByRole('button', { name: 'Ouvrir' });
    trigger.focus();
    fireEvent.click(trigger);

    fireEvent.keyDown(document, { key: 'Escape' });

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(document.activeElement).toBe(trigger);
  });
});
