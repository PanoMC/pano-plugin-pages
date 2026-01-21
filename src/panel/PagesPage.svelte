<article class="container vstack gap-3">
  <!-- Action Menu -->
  <PageActions>
    <div slot="right">
      <button type="button" class="btn btn-secondary" on:click={onCreateClick}>
        <i class="fas fa-plus"></i>
        <span class="d-lg-inline d-none ms-2"> {$_('pages.list.add-page')}</span>
      </button>
    </div>
  </PageActions>

  <div class="card">
    <CardHeader>
      <div slot="left">
        {$_('pages.list.total-count', { values: { count: data.pageCount || 0 } })}
      </div>
    </CardHeader>

    {#if !data.pages || data.pages.length === 0}
      <NoContent />
    {:else}
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
            <tr>
              <th scope="col" style="width: 60px;"></th>
              <th scope="col" style="width: 80px;">{$_('pages.list.table.id')}</th>
              <th scope="col">{$_('pages.list.table.title')}</th>
              <th scope="col">{$_('pages.list.table.url')}</th>
              <th scope="col">{$_('pages.list.table.status')}</th>
            </tr>
          </thead>
          <tbody>
            {#each data.pages as page (page.id)}
              <PageRow {page} {onEditClick} {onDeleteClick} />
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
    <div class="card-footer">
      <Pagination
        page={data.page}
        totalPage={data.totalPage}
        on:firstPageClick={() => onPageClick(1)}
        on:lastPageClick={() => onPageClick(data.totalPage)}
        on:pageLinkClick={(event) => onPageClick(event.detail.page)} />
    </div>
  </div>

  <ConfirmDeletePageModal />
</article>

<script context="module">
  import ApiUtil from '@panomc/sdk/utils/api';
  import {pluginId} from '../main';

  export async function load(event) {
    const {
      parent,
      url: { searchParams },
    } = event;
    const { pageTitle } = await parent();

    pageTitle.set(`plugins.${pluginId}.pages.list.title`);

    const page = searchParams.get('page') || 1;

    const body = await ApiUtil.get({
      path: `/api/panel/pages?page=${page}`,
      request: event,
    });

    if (body.error) {
      return { data: { pages: [], totalPage: 1, page: 1, pageCount: 0 } };
    }

    const data = body.data || body; // Handle both wrapped and unwrapped response
    data.page = parseInt(page);

    return { data: data };
  }
</script>

<script>
  import { base, goto } from '@panomc/sdk/svelte';
  import {
    PageActions,
    CardHeader,
    NoContent,
    Pagination,
  } from '@panomc/sdk/components';

  import { _ } from '../main';
  import PageRow from './components/PageRow.svelte';
  import ConfirmDeletePageModal, {
    show as showDeleteModal,
    setCallback as setDeleteCallback,
  } from './components/modals/ConfirmDeletePageModal.svelte';

  export let data;

  async function refreshData() {
    const pageNum = data.page === 1 ? '' : `?page=${data.page}`;
    await goto(`${base}/pages${pageNum}`, { invalidateAll: true });
  }

  async function onPageClick(pageNum) {
    data.page = pageNum;
    await refreshData();
  }

  function onCreateClick() {
    goto(`${base}/pages/create`);
  }

  function onEditClick(id) {
    goto(`${base}/pages/edit/${id}`);
  }

  function onDeleteClick(id) {
    const page = data.pages.find((p) => p.id === id);
    if (page) {
      showDeleteModal(page);
    }
  }

  setDeleteCallback(() => {
    refreshData();
  });
</script>
