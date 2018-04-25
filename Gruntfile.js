
module.exports = function(grunt) {

  grunt.initConfig({

    pkg: grunt.file.readJSON('package.json'),

    concat: {
      dist: {
        src: [
        	'bower_components/jquery/dist/jquery.min.js',
        	'bower_components/elliptic/dist/elliptic.min.js'
        ],
        dest: 'src/main/webapp/js/bundle.js'
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-concat');

  grunt.registerTask('default', ['concat']);
};
