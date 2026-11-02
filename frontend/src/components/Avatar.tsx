const HUES = [262, 168, 18, 205, 330, 45, 140, 290];

const hueFor = (seed: string) => {
  let hash = 0;
  for (let i = 0; i < seed.length; i += 1) hash = (hash * 31 + seed.charCodeAt(i)) | 0;
  return HUES[Math.abs(hash) % HUES.length];
};

export const initialsOf = (name: string) =>
  name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('') || '?';

interface Props {
  name: string;
  seed?: string;
  size?: 'sm' | 'md' | 'lg';
  online?: boolean;
}

export default function Avatar({ name, seed, size = 'md', online }: Props) {
  const hue = hueFor(seed ?? name);
  return (
    <span
      className={`avatar ${size}`}
      style={{ background: `hsl(${hue} 85% 92%)`, color: `hsl(${hue} 60% 28%)` }}
      aria-hidden="true"
    >
      {initialsOf(name)}
      {online && <span className="avatar-online" />}
    </span>
  );
}
