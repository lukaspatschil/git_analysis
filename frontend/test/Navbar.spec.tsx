import React from 'react';
import { describe, expect, it } from 'vitest';
import Wrapper from '../src/components/Wrapper';
import { render, screen } from './test-utils';

describe('Simple working test', () => {
  it('the title is visible', () => {
    render(<Wrapper>This is text</Wrapper>);
    expect(screen.getByText('This is text')).toBeInTheDocument();
  });
});