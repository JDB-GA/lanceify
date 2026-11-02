interface Props {
  ids: string[];
  names: Map<string, string>;
  limit?: number;
}

export default function SkillTags({ ids, names, limit = 6 }: Props) {
  if (ids.length === 0) return null;
  const shown = ids.slice(0, limit);
  return (
    <span className="tag-list">
      {shown.map((id) => <span key={id} className="tag">{names.get(id) ?? 'Skill'}</span>)}
      {ids.length > limit && <span className="tag more">+{ids.length - limit}</span>}
    </span>
  );
}
