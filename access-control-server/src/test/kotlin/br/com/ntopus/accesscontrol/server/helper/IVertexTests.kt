package br.com.ntopus.accesscontrol.server.helper

interface IVertexTests {
    fun addVertex()
    fun getVertexById()
    fun getVertexByCode()
    fun createVertexWithExtraProperty()
    fun cantCreateVertexThatExist()
    fun cantCreateVertexWithRequiredPropertyEmpty()
//    fun cantCreateEdgeWithSourceThatNotExist()
//    fun cantCreateEdgeWithTargetThatNotExist()
//    fun cantCreateEdgeWithIncorrectTarget()
//    fun createEdge()
//    fun updateProperty()
//    fun cantUpdateDefaultProperty()
//    fun cantUpdatePropertyFromVertexThatNotExist()
//    fun deleteVertex()
//    fun cantDeleteVertexThatNotExist()
}