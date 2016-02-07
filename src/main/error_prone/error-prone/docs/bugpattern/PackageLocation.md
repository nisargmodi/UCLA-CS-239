Java files should be located in a directory that matches the fully qualified
name of the package. For example, classes in the package
`edu.oswego.cs.dl.util.concurrent` should be located in:
`.../edu/oswego/cs/dl/util/concurrent`.

## Suppression

If necessary, the check may be suppressed by annotating the enclosing package
declaration with `@com.google.errorprone.annotations.SuppressPackageLocation`.
Note that package annotations must be located in a `package-info.java` file.
