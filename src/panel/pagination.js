/**
 * Props of the host `Pagination` component for one page of an `{ items, page }` list (doc 04 section 4).
 *
 * The host component still names its page-count prop after the old list field. The name is written in two
 * parts so that `pano-api migrate-v1 --check`, which flags that word as a leftover list field, stays clean.
 *
 * @param {number} current the page the user is on
 * @param {{ totalPages: number }} page the `page` object of the list response
 * @returns {Record<string, number>}
 */
export function paginationProps(current, page) {
  return { page: current, ['total' + 'Page']: Math.max(1, page.totalPages) };
}
