<article class:container={!data.page.resetLayout}>

  <!-- Temporary disabled -->
  {#if !data.page.resetLayout && data.page.showBreadcrumb}
    <nav aria-label="breadcrumb" class="mb-3 d-none">
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="/" class="text-decoration-none">Home</a></li>
        <li class="breadcrumb-item active" aria-current="page">{data.page.title}</li>
      </ol>
    </nav>
  {/if}

  <svelte:component this={PageTitle} title={data.page.title} />

  {#if data.page.htmlContent}
    {@html data.page.htmlContent}
  {:else}
    <NoContent />
  {/if}
</article>

<script context="module">
  import ApiUtil, { buildQueryParams } from '@panomc/sdk/utils/api';
  import { error, redirect } from '@panomc/sdk/svelte';

  export async function load(event) {
    const { params, parent } = event;
    const { pageTitle, session } = await parent();

    const url = event.url.pathname;

    // First try a direct match with the more efficient API
    const queryParams = buildQueryParams({ url });
    const res = await ApiUtil.get({
      path: `/api/pages/url${queryParams}`,
      request: event,
    });

    if (res.page) {
      if (res.page.loginRequired && !session?.user) {
        throw redirect(302, '/');
      }

      pageTitle.set(res.page.title);
      return { data: { page: res.page } };
    }

    // Fallback: Fetch all active pages to check for dynamic parameter matches (:param)
    const allRes = await ApiUtil.get({
      path: '/api/pages',
      request: event,
    });

    if (allRes.status === 'SUCCESS' && allRes && allRes.pages) {
      const page = allRes.pages.find((p) => {
        const cleanUrl = url.replace(/\/$/, '') || '/';
        const cleanPUrl = p.url.replace(/\/$/, '') || '/';

        if (cleanPUrl.includes(':')) {
          const parts = cleanPUrl.split('/');
          const urlParts = cleanUrl.split('/');
          if (parts.length !== urlParts.length) return false;
          return parts.every((part, i) => part.startsWith(':') || part === urlParts[i]);
        }
        return false; // Direct matches already handled above
      });

      if (page) {
        if (page.loginRequired && !session?.user) {
          throw redirect(302, '/');
        }

        pageTitle.set(page.title);
        return { data: { page } };
      }
    }

    return error(404, 'Page not found');
  }
</script>

<script>
  import { NoContent } from '@panomc/sdk/components';
  import { getPanoContext } from '@panomc/sdk/internal';

  export let data;

  const { PageTitle } = getPanoContext().context.components;
</script>
