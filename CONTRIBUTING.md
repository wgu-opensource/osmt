# How to Contribute
Never made an open source contribution before? Wondering how contributions work in our project? Here's a quick rundown!

## Code of Conduct
This project adheres to our [code of conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## Issue Tracking
We use GitHub's [Issue Tracker](https://github.com/wgu-opensource/osmt/issues).
- You can submit issues for this project. Please do a courtesy search to verify that your issue is not already being tracked. If you have additional context for an existing issue, please add that with comments.
- When you are looking for issues to work on, please notice if the issue already has an "Assignee". We are still developing how to manage ownership of a given issue.
- Issues have labels describing what kind of changes are involved. GitHub search makes it easy to use these labels to find issues or pull requests you're interested in. Each label is listed with search links for finding open items with that label. We encourage you to read about other search filters to help you write more focused queries.

- If you don't feel ready to make a code contribution yet, no problem! Check out the [existing issues](https://github.com/wgu-opensource/osmt/issues) to see if an issue exists already for the change you want.

### Expectations for contributed code
- OSMT project uses Kotlin 1.3.72 and Angular Front-end 10.0, with MySQL DB, redis and Elasticsearch. See the [Getting Started](README.md#getting-started) section in the README file.
- Contributed code needs to follow appropriate style guides. Coding style isn't only a matter of preference, but is essential in managing an effective branching and release strategy. Trivial or unrelated code changes create merge conflicts, and introduce risk and wasted time in resolving them.
  - Kotlin - https://developer.android.com/kotlin/style-guide
  - Angular - https://angular.io/guide/styleguide
- All code changes should be relevant to the issue/feature at hand. Avoid unrelated changes, like IDE import sorting or unrelated whitespace changes.
  - Changes that are ["boy scouting"](https://headspring.com/2020/01/27/clean-code-conundrum/) (code improvements that leave an area cleaner than you found it) should be separate from feature changes. Boy scouting changes may be disruptive to others, and should be coordinated with project maintainers.
- All execution paths for new code should be covered by unit tests.
  - Many IDEs will report on unit test coverage and call out any gaps. That said, reporting of test coverage doesn't mean that the tests are useful or effective. If you find a section of code is difficult to unit test, this may indicate the need for some refactoring. Non-trivial refactors should also be coordinated with project maintainers.

### Using git with this project
1. Use the project's [Issue Tracker](https://github.com/wgu-opensource/osmt/issues) to find an issue that you want to address, or a feature that you would like to add.
2. Clone the repository to your local machine using `git clone https://github.com/wgu-opensource/osmt.git` or `git@github.com:wgu-opensource/osmt.git` for SSH.
3. Create a new branch for your fix using `git checkout origin/develop -b your-local-branch-name`.
   - Note, make sure you only branch from `origin/develop`!
4. Make the appropriate changes for the issue you are trying to address, or the feature that you want to add. Include appropriate test coverage.
5. Use `git add insert-paths-of-changed-files-here` to stage the changed files for the commit.
6. Use `git commit` to commit the contents of the index. This should open an editor; please provide a useful commit message (see below for [more about commit messages](#commit-message-format))
7. On GitHub, OSMT uses a branching workflow, where committers create a feature branch containing the desired changes (rather than asking contributors to fork our GitHub repo). Push the changes to a feature branch in GitHub using `git push HEAD:origin feature/your-feature-branch-name`.
  - A given branch should only have a single feature. Multiple unrelated features should handled by creating multiple feature branches.

### Submit a Pull Request (PR)
A ["Pull Request"](https://docs.github.com/en/github/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-pull-requests) initiates a code review workflow in which OSMT project maintainers review your contributed code, and request additional changes or approve it for merging into a coming release.
1. After pushing your commit to a feature branch on GitHub, please review it for any unexpected changes. When you are ready for a review, you can submit a pull request (GitHub will provide a button for this in the browser after you push).
   - Provide a title for your pull request. It can be the same as the 1st line of your commit message.
   - Provide a description for your pull request. It can be the same as the description in your commit message.
   - It's OK if your pull request is not perfect (no pull request is), the reviewer will be able to help you fix any problems and improve it!
2. Wait for the pull request to be reviewed by a maintainer.
3. Make changes to the pull request if the reviewing maintainer recommends them. Push those changes to the same GitHub feature branch.
4. Celebrate your success after your pull request is merged!

### Commit Message Format
Please format your commit messages with a summary line (50 characters or less), a blank line, and more detailed text explaining the commit.
- Understand ahead of time that commits are often squashed, so commit messages may not last forever.
- Speak to both others and your near-future self. Capture enough ephemeral context to understand why this commit exists.
- Reference [GitHub issues](https://guides.github.com/features/issues/) whenever possible.

#### Example commit message:
_(with thanks to https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html)_
```
Capitalized, short (50 chars or less) summary #

More detailed explanatory text, if necessary.  Wrap it to about 72
characters or so.  In some contexts, the first line is treated as the
subject of an email and the rest of the text as the body.  The blank
line separating the summary from the body is critical (unless you omit
the body entirely); tools like rebase can get confused if you run the
two together.

Write your summary line in the imperative: "Fix bug" and not "Fixed bug"
or "Fixes bug."  This convention matches up with commit messages generated
by commands like git merge and git revert.

Further paragraphs come after blank lines.

- Bullet points are okay, too

- Typically a hyphen or asterisk is used for the bullet, followed by a
  single space, with blank lines in between, but conventions vary here

- Use a hanging indent
```

### Tips
- Keep changes small and focused. In the case of multiple bug fixes or unrelated features, create one branch and PR per feature. This makes it easier to review, merge, and potentially rollback changes.
- Avoid unrelated code changes, even in a changed file. Imports and whitespace formatting are frequent unrelated changes.
- Test coverage, test coverage, test coverage. 

# Where can I go for help?
Make sure you follow the README to clone the project and setup. If you need help, you can ask questions in our GitHub '[Discussions](https://github.com/wgu-opensource/osmt/discussions)' area.
