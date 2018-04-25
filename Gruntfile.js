
module.exports = function(grunt) {

  grunt.initConfig({

    pkg: grunt.file.readJSON('package.json'),
    
    browserify: {
      elliptic: {
        src: ['bower_components/elliptic/lib/elliptic.js'],
        dest: 'src/main/webapp/js/elliptic.js',
        options: {
          require: ['elliptic']
        }
      },
      hashjs: {
          src: ['bower_components/hash.js/lib/hash.js'],
          dest: 'src/main/webapp/js/hash.js',
          options: {
            require: ['hash.js']
          }
        }
    },
    
    concat: {
      bundle: {
        src: [
          'bower_components/jquery/dist/jquery.min.js',
          'bower_components/elliptic/dist/elliptic.min.js'
        ],
        dest: 'src/main/webapp/js/bundle.js'
      }
    }
  });

  grunt.loadNpmTasks('grunt-browserify');
  grunt.loadNpmTasks('grunt-contrib-concat');

  grunt.registerTask('default', ['browserify', 'concat']);

};
