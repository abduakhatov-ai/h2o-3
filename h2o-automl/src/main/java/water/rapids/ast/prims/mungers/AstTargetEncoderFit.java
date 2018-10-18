package water.rapids.ast.prims.mungers;

import ai.h2o.automl.targetencoding.BlendingParams;
import ai.h2o.automl.targetencoding.TargetEncoder;
import water.fvec.Frame;
import water.rapids.Env;
import water.rapids.Val;
import water.rapids.ast.AstBuiltin;
import water.rapids.ast.AstRoot;
import water.rapids.ast.params.AstStrList;
import water.rapids.vals.ValMapFrame;

import java.util.Map;

/**
 * Rapids wrapper for java TargetEncoder
 */
public class AstTargetEncoderFit extends AstBuiltin<AstTargetEncoderFit> {
  @Override
  public String[] args() {
    return new String[]{"trainFrame teColumns targetColumnName foldColumnName"};
  }

  @Override
  public String str() {
    return "target.encoder.fit";
  }

  @Override
  public int nargs() {
    return 1 + 4;
  }

  @Override
  public ValMapFrame apply(Env env, Env.StackHelp stk, AstRoot asts[]) {

    Frame trainFrame = getTrainingFrame(env, stk, asts);
    String[] teColumnsToEncode = getTEColumns(asts);
    String targetColumnName = getTargetColumnName(env, stk, asts);
//    double numberOfFolds = getNumberOfFolds(env, stk, asts); // TODO make it optional
    String foldColumnName = getFoldColumnName(env, stk, asts); // TODO make it optional
    boolean withImputationForOriginalColumns = true; // Default fo now

    BlendingParams params = new BlendingParams(3, 1);

    TargetEncoder tec = new TargetEncoder(teColumnsToEncode, params);

    Map<String, Frame> encodingMap = tec.prepareEncodingMap(trainFrame, targetColumnName, foldColumnName, withImputationForOriginalColumns);

    return new ValMapFrame(encodingMap);
  }

  private Frame getTrainingFrame(Env env, Env.StackHelp stk, AstRoot asts[]) {
    return stk.track(asts[1].exec(env)).getFrame();
  }

  private String[] getTEColumns(AstRoot asts[]) {

    if (asts[2] instanceof AstStrList) {
      AstStrList teColumns = ((AstStrList) asts[2]);
      return teColumns._strs;
    }
    else throw new IllegalStateException("Couldn't parse `teColumns` parameter");
  }

  private String getTargetColumnName(Env env, Env.StackHelp stk, AstRoot asts[]) {
    return stk.track(asts[3].exec(env)).getStr();
  }

  private double getNumberOfFolds(Env env, Env.StackHelp stk, AstRoot asts[]) {
    return stk.track(asts[4].exec(env)).getNum();
  }

  private String getFoldColumnName(Env env, Env.StackHelp stk, AstRoot asts[]) {
    return stk.track(asts[4].exec(env)).getStr();
  }

  private boolean getWithImputation(Env env, Env.StackHelp stk, AstRoot asts[]) {
    return stk.track(asts[5].exec(env)).getNum() == 1;
  }

}
