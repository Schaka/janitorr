module.exports = {
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
    'header-max-length': [2, 'always', 100]
  },
  ignores: [
    (message) => message.startsWith('Initial plan'),
    (message) => message.startsWith('WIP'),
    (message) => message.startsWith('Merge pull request')
  ]
};
