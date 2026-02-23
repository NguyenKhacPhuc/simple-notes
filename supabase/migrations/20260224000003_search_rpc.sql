create or replace function public.search_notes(query text)
returns setof public.notes
language sql stable security definer
set search_path = '' as $$
  select * from public.notes
  where user_id = auth.uid()
    and is_deleted = false
    and fts @@ plainto_tsquery('english', query)
  order by ts_rank(fts, plainto_tsquery('english', query)) desc
  limit 50;
$$;
