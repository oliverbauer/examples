uniform mat4 g_WorldViewProjectionMatrix;

uniform vec4 m_TopColor;
uniform vec4 m_FrontColor;
uniform vec4 m_RightColor;
uniform vec4 m_BackColor;
uniform vec4 m_LeftColor;
uniform vec4 m_BottomColor;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec4 varColor;

void main()
{
  gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);

  if (inNormal.y > 0.0) {
    varColor = m_TopColor;
  } else if (inNormal.y < 0.0) {
    varColor = m_BottomColor;
  } else if (inNormal.x > 0.0) {
    varColor = m_RightColor;
  } else if (inNormal.x < 0.0) {
    varColor = m_LeftColor;
  } else if (inNormal.z > 0.0) {
    varColor = m_FrontColor;
  } else if (inNormal.z < 0.0) {
    varColor = m_BackColor;
  } else {
    varColor = vec4(1.0, 1.0, 1.0, 1.0);
  }
}