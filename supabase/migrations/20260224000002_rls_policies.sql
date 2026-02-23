alter table public.notes enable row level security;

create policy "Users read own notes"  on public.notes for select using (auth.uid() = user_id);
create policy "Users insert own notes" on public.notes for insert with check (auth.uid() = user_id);
create policy "Users update own notes" on public.notes for update using (auth.uid() = user_id);
create policy "Users delete own notes" on public.notes for delete using (auth.uid() = user_id);
