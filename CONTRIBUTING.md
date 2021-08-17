# Overview

# How to Contribute
## Submit an Issue

## Submit a Pull Request (PR)
We use a branching workflow, where committers create a feature branch containing the desired changes.
- Name the feature branch `feature/<feature_name>`, where "feature_name" is a very brief description.
- Base your feature branch on "origin/develop".
- Branches should be limited to a single feature, multiple unrelated features should handled by creating multiple feature branches. 
- Code will follow appropriate style guides (See links for Kotlin and Angular style guides below).
- All executions paths for new code should be covered by a unit tests.
- More details in [How do I make a Contribution?](#how-do-i-make-a-contribution), below.

### Workflow

### Style Guide
- Kotlin - https://developer.android.com/kotlin/style-guide
- Angular - https://angular.io/guide/styleguide

### Commit Message Format
Please format your commit messages with a summary line (50 characters or less), a blank line, and more detailed text explaining the commit.
- Understand ahead of time that commits are often squashed, so commit messages may not last forever.
- Speak to both others and your near-future self. Capture enough ephemeral context to understand why this commit exists.
- Reference issues and tickets

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

## Where to go for help
Make sure you follow the README to clone the project and setup

## What do I need to know to help?
If you want to help to with a code contribution, our project uses Kotlin 1.3.72 and Angular Front-end 10.0, with MySQL DB, redis and Elasticsearch. If you don't feel ready to make a code contribution yet, no problem! Check out the [existing issues](https://github.com/wgu-opensource/osmt/issues) to see if an issue exists already for the change you want.

## How do I make a contribution?
Never made an open source contribution before? Wondering how contributions work in our project? Here's a quick rundown!

1. Find an issue that you are interested in addressing, or a feature that you would like to add.
2. Clone the repository to your local machine using `git clone https://github.com/wgu-opensource/osmt.git` or `git@github.com:wgu-opensource/osmt.git` for SSH.
3. Create a new branch for your fix using `git checkout origin/develop -b your-local-branch-name`.
   - Note, make sure you only branch off `origin/develop`!
4. Make the appropriate changes for the issue you are trying to address, or the feature that you want to add.
5. Use `git add insert-paths-of-changed-files-here` to add the file contents of the changed files to the "snapshot" git uses to manage the state of the project, also known as the index.
6. Use `git commit` to commit the contents of the index. This should open an editor; please provide a useful commit message (see above for [more about commit messages](#commit-message-format))
7. Push the changes to the remote repository using `git push HEAD:origin feature/your-feature-branch-name`. 
8. Submit a pull request to the upstream repository.
9. Provide a title for your pull request. It can be the same as the 1st line of your commit message.
10. Provide a description for your pull request. It can be the same as the description in your commit message.
11. It's OK if your pull request is not perfect (no pull request is), the reviewer will be able to help you fix any problems and improve it!
12. Wait for the pull request to be reviewed by a maintainer.
13. Make changes to the pull request if the reviewing maintainer recommends them. Push those changes to the same feature branch.
14. Celebrate your success after your pull request is merged!

# Where can I go for help?
If you need help, you can ask questions on our mailing list, IRC chat, or
[list any other communication platforms that your project uses].