import { useState } from 'react';
import { ApiError } from '../api/client';
import { proposalsApi } from '../api/proposals';
import { useToast } from '../context/ToastContext';
import type { Proposal } from '../types';

export function useProposalDecisions(reload: () => Promise<void>) {
  const { notify } = useToast();
  const [busyId, setBusyId] = useState<string | null>(null);

  const decide = async (proposal: Proposal, accept: boolean) => {
    if (busyId) return;
    const prompt = accept
      ? 'Hire this freelancer? All other pending proposals for this project will be declined.'
      : 'Decline this proposal?';
    if (!window.confirm(prompt)) return;
    setBusyId(proposal.id);
    try {
      await (accept ? proposalsApi.accept(proposal) : proposalsApi.reject(proposal));
      notify(accept ? 'Freelancer hired. Other proposals were declined.' : 'Proposal declined');
    } catch (err) {
      const apiError = err instanceof ApiError ? err : null;
      notify(apiError?.message ?? 'Something went wrong', 'error');
    } finally {
      setBusyId(null);
      await reload();
    }
  };

  return {
    busyId,
    accept: (proposal: Proposal) => decide(proposal, true),
    reject: (proposal: Proposal) => decide(proposal, false),
  };
}
