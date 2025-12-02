import { PaginatedResponse } from '../types/PaginationTypes';

export function formatDateTime(value: string | null | undefined): string | null {
  if (!value) return null;

  const date = new Date(value);
  if (isNaN(date.getTime())) return value.toString();

  return date.toLocaleString('de-DE', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

export function formatDateFields(
  result: PaginatedResponse,
  dateFields: string[]
): PaginatedResponse {
  const formattedItems = result.items.map((item: Record<string, any>) => {
    const copy: any = { ...item };
    for (const field of dateFields) {
      if (field in copy) {
        copy[field] = formatDateTime(copy[field]);
      }
    }
    return copy;
  });

  return {
    ...result,
    items: formattedItems,
  };
}
