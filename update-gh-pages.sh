echo -e "STARTING CHECK...\n"
if [ "$TRAVIS_PULL_REQUEST" == "true" ]; then
  echo -e "Starting to update gh-pages\n"

  #copy data we're interested in to other place
  cp -R testng $HOME/build/cirej2000/PayoffWeatherTestBVT/target/surefire-reports

  #go to home and setup git
  cd $HOME
  git config --global user.email "cirej2013@gmail.com"
  git config --global user.name "cirej2000"

  #using token clone gh-pages branch
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/cirej2000/PayoffWeatherTestBVT.git  gh-pages > /dev/null

  #go into diractory and copy data we're interested in to that directory
  cd gh-pages
  cp -Rf $HOME/testng/* .

  #add, commit and push files
  git add -f .
  git commit -m "Travis test results for $TRAVIS_BUILD_NUMBER pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Done magic with coverage\n"
fi