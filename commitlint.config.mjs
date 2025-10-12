export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'build',
        'chore',
        'ci',
        'docs',
        'feat',
        'fix',
        'perf',
        'refactor',
        'revert',
        'style',
        'test'
      ]
    ],
    'subject-case': [2, 'never', ['upper-case']],
    'header-max-length': [0], // Disabled - no limit
    'body-max-line-length': [0], // Disabled - no limit  
    'footer-max-line-length': [0] // Disabled - no limit
  },
  ignores: [
    (message) => message.startsWith('Initial plan'),
    (message) => message.startsWith('WIP'),
    (message) => message.startsWith('Merge pull request')
  ]
};