package scalismo.ui.vtk

import scalismo.ui.VectorField.VectorFieldRenderable3D
import scalismo.ui.visualization.props.ColorProperty
import vtk._

class VectorFieldActor3D(renderable: VectorFieldRenderable3D) extends PolyDataActor with ColorableActor {
  private lazy val arrow = new vtkArrowSource

  override lazy val color = new ColorProperty(None)
  override lazy val opacity = renderable.opacity

  val points = new vtkPoints
  val vectors = new vtkFloatArray() {
    SetNumberOfComponents(3)
  }

  val scalars = new vtkFloatArray() {
    SetNumberOfComponents(1)
  }

  {
    // for the colors to be correctly displayed, we need to normalize the vector norms as scalars onto [0,1]
    val norms = Array.ofDim[Double](renderable.source.peer.domain.points.length)

    var i = 0
    renderable.source.peer.pointsWithValues.foreach {
      case (point, vector) =>
        points.InsertNextPoint(point(0), point(1), point(2))
        vectors.InsertNextTuple3(vector(0), vector(1), vector(2))
        norms(i) = vector.norm
        i += 1
    }

    if (norms.length > 0) {
      val max = norms.max
      for (j <- 0 until norms.length) {
        scalars.InsertNextValue(norms(j) / max)
      }
    }
  }

  val polydata = new vtkPolyData {
    SetPoints(points)
    GetPointData().SetVectors(vectors)
    GetPointData().SetScalars(scalars)
  }

  val glyph = new vtkGlyph3D {
    SetSourceConnection(arrow.GetOutputPort)
    SetInputData(polydata)
    //    ScalingOn()
    OrientOn()
    SetScaleModeToScaleByVector()
    SetVectorModeToUseVector()
    SetColorModeToColorByScalar()
  }

  mapper.SetInputConnection(glyph.GetOutputPort)
  mapper.ScalarVisibilityOn()

  this.GetProperty().SetInterpolationToGouraud()
  setGeometry()

  def setGeometry() = this.synchronized {
    arrow.Modified()
    glyph.Update()
    glyph.Modified()
    mapper.Modified()
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    super.onDestroy()
    glyph.Delete()
    polydata.Delete()
    vectors.Delete()
    points.Delete()
    arrow.Delete()
  }
}