-- SQL migration for TBE-2232: Update grid configs for new package names
UPDATE p28_cfg_lean_grid_config
SET grid_prefs = decode(
    replace(
        encode(grid_prefs, 'escape'),
        E'\\013ui.UIFilter',
        E'\\024t9t.base.ui.UIFilter'
    ),
    'escape'
)
WHERE position(
    decode(E'\\013ui.UIFilter', 'escape') IN grid_prefs
) > 0;
