import { useState, useEffect, useRef } from 'react';
import { skillApi, type Skill } from '@/services/skillApi';

interface SkillSearchProps {
  value: number | null;
  onChange: (skillId: number, skillName: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

export function SkillSearch({ value, onChange, placeholder = '搜索技能', disabled = false }: SkillSearchProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [skills, setSkills] = useState<Skill[]>([]);
  const [loading, setLoading] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (value) {
      skillApi.getSkillById(value)
        .then(skill => {
          setSearchTerm(skill.skillName);
        })
        .catch(() => {
          setSearchTerm('');
        });
    } else {
      setSearchTerm('');
    }
  }, [value]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setShowDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleSearch = async (term: string) => {
    setSearchTerm(term);
    if (term.trim().length < 2) {
      setSkills([]);
      return;
    }

    setLoading(true);
    try {
      const results = await skillApi.searchSkills(term);
      setSkills(results);
      setShowDropdown(true);
    } catch (error) {
      console.error('搜索技能失败:', error);
      setSkills([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectSkill = (skill: Skill) => {
    setSearchTerm(skill.skillName);
    setShowDropdown(false);
    onChange(skill.skillId, skill.skillName);
  };

  return (
    <div className="relative w-full" ref={dropdownRef}>
      <div className="relative">
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => handleSearch(e.target.value)}
          onFocus={() => searchTerm.length >= 2 && setShowDropdown(true)}
          placeholder={placeholder}
          disabled={disabled}
          className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
        />
        {loading && (
          <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-paper-400">
            <div className="w-4 h-4 border-2 border-paper-400 border-t-transparent rounded-full animate-spin"></div>
          </div>
        )}
      </div>

      {showDropdown && skills.length > 0 && (
        <div className="absolute top-full left-0 right-0 mt-1 max-h-60 overflow-y-auto bg-white rounded-lg border-2 border-paper-300 shadow-lg z-10">
          {skills.map((skill) => (
            <div
              key={skill.skillId}
              className="px-3 py-2 hover:bg-paper-100 cursor-pointer transition-colors"
              onClick={() => handleSelectSkill(skill)}
            >
              <div className="font-medium text-paper-800">{skill.skillName}</div>
              <div className="text-xs text-paper-500">{skill.category}</div>
            </div>
          ))}
        </div>
      )}

      {showDropdown && skills.length === 0 && !loading && (
        <div className="absolute top-full left-0 right-0 mt-1 p-3 bg-white rounded-lg border-2 border-paper-300 shadow-lg z-10">
          <p className="text-sm text-paper-500">未找到匹配的技能</p>
        </div>
      )}
    </div>
  );
}
