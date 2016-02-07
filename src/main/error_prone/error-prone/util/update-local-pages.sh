# https://github.com/google/dagger/blob/master/util/generate-latest-docs.sh

echo -e "Publishing docs...\n"

GH_PAGES_DIR=$HOME/gh-pages

(
cd $GH_PAGES_DIR
rm -rf _data/bugpatterns.yaml bugpattern
mkdir -p _data bugpattern api/latest
)

# The "mvn clean" is necessary since the wiki docs are generated by an
# annotation processor that also compiles the code.  If Maven thinks the code
# does not need to be recompiled, the wiki docs will not be generated either.
mvn clean

mvn -P run-annotation-processor compile site
rsync -a core/target/generated-wiki/ ${GH_PAGES_DIR}
