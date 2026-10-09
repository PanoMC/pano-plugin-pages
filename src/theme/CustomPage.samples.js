// Sample data of CustomPage for the view catalogue (doc 02 section 7). Pure data: import only view helpers and
// relative .js fixtures.
/** @type {string[]} */
export const notApplicable = ['error', 'loading'];

/** @type {import('@panomc/plugin-kit').Samples} */
export default {
  filled: {
    props: {
      data: {
        page: {
          title: 'Rules',
          url: '/rules',
          htmlContent: '<h2>Server rules</h2><p>Be kind. No griefing. Have fun.</p>',
          resetLayout: false,
          showBreadcrumb: false,
        },
      },
    },
  },
  empty: {
    props: {
      data: { page: { title: 'Rules', url: '/rules', htmlContent: '', resetLayout: false, showBreadcrumb: false } },
    },
  },
};
