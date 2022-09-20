import { describe, expect, it } from 'vitest'
import App from '../src/App'
import { render, screen, userEvent } from './test-utils'

describe('Simple working test', () => {
  it('the title is visible', () => {
    render(<App />)
    expect(screen.getByText('Click on the Vite and React logos to learn more')).toBeInTheDocument()
  })

  it('should increment count on click', async () => {
    render(<App />)
    userEvent.click(screen.getByRole('button'))
    expect(await screen.findByText(/count is 1/i)).toBeInTheDocument()
  })
})