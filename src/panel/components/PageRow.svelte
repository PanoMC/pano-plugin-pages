<tr>
  <th scope="row" class="align-middle text-center" style="width: 60px;">
    <div class="dropdown position-static">
      <button
        type="button"
        class="btn btn-link"
        aria-expanded="false"
        aria-haspopup="true"
        data-bs-toggle="dropdown"
        use:tooltip={[$_('pages.list.table.actions')]}
        aria-label={$_('pages.list.table.actions')}>
        <span class="fas fa-ellipsis-v"></span>
      </button>
      <div class="dropdown-menu dropdown-menu-start animate__animated animate__fadeIn">
        <button type="button" class="dropdown-item" on:click={() => onEditClick(page.id)}>
          <span>
            <i class="fas fa-edit me-2"></i>
            {$_('pages.list.table.actions-edit')}
          </span>
        </button>
        <button type="button" class="dropdown-item" on:click={() => onDeleteClick(page.id)}>
          <i class="fas fa-trash me-2"></i>
          <span> {$_('pages.list.table.actions-delete')} </span>
        </button>
      </div>
    </div>
  </th>
  <td class="align-middle" style="width: 80px;">
    <code>#{page.id}</code>
  </td>
  <td class="align-middle">
    <button
      type="button"
      on:click={() => onEditClick(page.id)}
      use:tooltip={[$_('pages.list.table.actions-edit')]}
      title={page.title}
      aria-label={page.title}
      class="btn btn-link p-0 text-start text-decoration-none w-100 text-truncate focus-ring">
      {page.title}
    </button>
  </td>
  <td class="align-middle">{page.linkName || '-'}</td>
  <td class="align-middle text-muted small">{page.url}</td>
  <td class="align-middle">
    {#if page.active}
      <span class="badge text-bg-success">
        {$_('pages.list.status.active')}
      </span>
    {:else}
      <span class="badge text-bg-danger">
        {$_('pages.list.status.passive')}
      </span>
    {/if}
  </td>
</tr>

<script>
  import { _ } from '../../main';
  import tooltip from '@panomc/sdk/utils/tooltip';

  export let page;
  export let onEditClick;
  export let onDeleteClick;
</script>
