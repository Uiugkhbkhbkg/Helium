#define HIGH

uniform sampler2D u_texture;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform vec2 u_offset;

uniform float u_time;
uniform float u_stroke;
uniform float u_alpha;

varying vec2 v_texCoords;

const float threshold = 0.4;

void main() {
    vec2 v = u_stroke*(1.0 / u_resolution);

    vec4 base = texture2D(u_texture, v_texCoords);

    float m = min(min(min(min(min(min(min(
             texture2D(u_texture, v_texCoords + vec2(1.0, 0.0) * v).a,
             texture2D(u_texture, v_texCoords + vec2(0.7071, 0.7071) * v).a),
             texture2D(u_texture, v_texCoords + vec2(0.0, 1.0) * v).a),
             texture2D(u_texture, v_texCoords + vec2(-0.7071, 0.7071) * v).a),
             texture2D(u_texture, v_texCoords + vec2(-1.0, 0.0) * v).a),
             texture2D(u_texture, v_texCoords + vec2(-0.7071, -0.7071) * v).a),
             texture2D(u_texture, v_texCoords + vec2(0.0, -1.0) * v).a),
             texture2D(u_texture, v_texCoords + vec2(0.7071, -0.7071) * v).a);

    if(base.a > threshold && m < threshold) {
        gl_FragColor = vec4(base.rgb, 1.0);
    }
    else if (base.a > threshold){
        gl_FragColor = vec4(base.rgb, u_alpha);
    }
    else {
        gl_FragColor = vec4(0.0);
    }
}
