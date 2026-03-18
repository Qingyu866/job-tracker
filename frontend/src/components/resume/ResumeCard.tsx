import { FileText, Briefcase, User, Check, Star } from 'lucide-react';
import type { UserResume } from '@/types/resume';

export interface ResumeCardProps {
  resume: UserResume;
  selected?: boolean;
  onSelect?: () => void;
  onEdit: () => void;
  onDelete: () => void;
  onSetDefault?: () => void;
}

export function ResumeCard({
  resume,
  selected = false,
  onSelect,
  onEdit,
  onDelete,
  onSetDefault,
}: ResumeCardProps) {
  const skillNames = resume.skills?.map(s => s.name) || [];

  return (
    <div
      className={`rounded-xl border-2 shadow-paper hover:shadow-paper-md transition-all cursor-pointer overflow-hidden relative hover:bg-[#f5f0e6] ${
        selected ? 'border-accent-amber ring-2 ring-accent-amber/20' : 'border-paper-400 hover:border-paper-500'
      }`}
      onClick={onSelect}
    >
      {selected && (
        <div className="absolute top-3 right-3 z-10">
          <div className="w-6 h-6 bg-accent-amber rounded-full flex items-center justify-center">
            <Check className="w-4 h-4 text-white" />
          </div>
        </div>
      )}

      <div className="p-4">
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-2">
            <FileText className="w-5 h-5 text-paper-400" />
            <h3 className="text-paper-700 font-medium">{resume.resumeName}</h3>
          </div>
          {resume.isDefault && (
            <span className="px-2 py-0.5 text-xs font-medium bg-accent-amber/20 text-accent-amber rounded-full flex items-center gap-1">
              <Star className="w-3 h-3" />
              默认
            </span>
          )}
        </div>

        <div className="space-y-2 mb-3">
          {resume.workYears !== undefined && (
            <div className="flex items-center gap-2 text-sm text-paper-600">
              <Briefcase className="w-4 h-4" />
              <span>{resume.workYears} 年经验</span>
            </div>
          )}
          {resume.currentPosition && (
            <div className="flex items-center gap-2 text-sm text-paper-600">
              <User className="w-4 h-4" />
              <span>{resume.currentPosition}</span>
            </div>
          )}
        </div>

        {skillNames.length > 0 && (
          <div className="flex flex-wrap gap-1.5 mb-3">
            {skillNames.slice(0, 4).map((skill, index) => (
              <span
                key={index}
                className="px-2 py-0.5 text-xs bg-paper-100 text-paper-600 rounded-full"
              >
                {skill}
              </span>
            ))}
            {skillNames.length > 4 && (
              <span className="px-2 py-0.5 text-xs bg-paper-100 text-paper-500 rounded-full">
                +{skillNames.length - 4}
              </span>
            )}
          </div>
        )}

        <div className="flex items-center gap-2 text-paper-400 text-xs">
          <span>创建于 {new Date(resume.createdAt).toLocaleDateString()}</span>
        </div>
      </div>

      <div className="border-t border-paper-200 p-3 bg-paper-50 flex justify-end gap-2">
        {!resume.isDefault && onSetDefault && (
          <button
            onClick={(e) => {
              e.stopPropagation();
              onSetDefault();
            }}
            className="px-3 py-1.5 text-xs font-medium text-paper-600 hover:bg-paper-100 rounded-lg transition-colors"
          >
            设为默认
          </button>
        )}
        <button
          onClick={(e) => {
            e.stopPropagation();
            onEdit();
          }}
          className="px-3 py-1.5 text-xs font-medium text-paper-600 hover:bg-paper-100 rounded-lg transition-colors"
        >
          编辑
        </button>
        <button
          onClick={(e) => {
            e.stopPropagation();
            onDelete();
          }}
          className="px-3 py-1.5 text-xs font-medium text-accent-red hover:bg-accent-red/10 rounded-lg transition-colors"
        >
          删除
        </button>
      </div>
    </div>
  );
}
