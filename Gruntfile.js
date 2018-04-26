module.exports = function(grunt) {

  grunt.initConfig({

    pkg : grunt.file.readJSON('package.json'),

    browserify : {
      bundle : {
        files : {
          'build/js/bundle.js' : [ 'bower_components/*/lib/*.js' ]
        },
        options : {
          require : [ 'elliptic', 'hash.js' ]
        }
      }
    },

    uglify : {
      bundle : {
        files : {
          'build/js/bundle.min.js' : [ 'build/js/bundle.js' ]
        }
      }
    },

    concat : {
      bundle : {
        files : {
          'src/main/webapp/js/bundle.js' : [ 'bower_components/jquery/dist/jquery.min.js', 'build/js/bundle.min.js' ]
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-browserify');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-concat');

  grunt.registerTask('default', [ 'browserify', 'uglify', 'concat' ]);
};
