import { describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { DataTable } from './DataTable';

const columns = [
  { key: 'nom', label: 'Nom', sortable: true },
  { key: 'ville', label: 'Ville' },
];

const data = [
  { id: 1, nom: 'Douala Central', ville: 'Douala' },
  { id: 2, nom: 'Bafoussam Nord', ville: 'Bafoussam' },
];

describe('DataTable — tri accessible', () => {
  it('annonce l’état de tri courant puis son inversion', () => {
    render(
      <DataTable columns={columns} data={data} keyExtractor={(item) => item.id as number} />,
    );

    const header = screen.getByRole('columnheader', { name: 'Nom' });
    expect(header).toHaveAttribute('aria-sort', 'none');

    fireEvent.click(screen.getByRole('button', { name: 'Nom' }));
    expect(header).toHaveAttribute('aria-sort', 'ascending');

    fireEvent.click(screen.getByRole('button', { name: 'Nom' }));
    expect(header).toHaveAttribute('aria-sort', 'descending');
  });
});
