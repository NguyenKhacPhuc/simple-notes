create table public.notes (
  id          uuid primary key default gen_random_uuid(),
  user_id     uuid not null references auth.users(id) on delete cascade,
  title       text not null default '',
  content     text not null default '',
  is_deleted  boolean not null default false,
  sync_version bigint not null default 1,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  fts         tsvector generated always as (
    setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(content, '')), 'B')
  ) stored
);

create index idx_notes_user_id    on public.notes(user_id);
create index idx_notes_updated_at on public.notes(updated_at);
create index idx_notes_fts        on public.notes using gin(fts);

create or replace function public.set_updated_at()
returns trigger as $$ begin
  new.updated_at = now();
  new.sync_version = old.sync_version + 1;
  return new;
end; $$ language plpgsql;

create trigger trg_notes_updated_at
  before update on public.notes
  for each row execute function public.set_updated_at();
